# Remplit la base microfinance avec un jeu de données de test, en passant par l'API (les règles métier s'appliquent).
# Les dates sont calculées à partir d'aujourd'hui : on obtient des prêts à jour, en retard et soldés.
# Prérequis : base vide (creer-bases.sql) et application démarrée.
# Lancement : powershell -ExecutionPolicy Bypass -File donnees\remplir-base.ps1
param([string]$Url = "http://localhost:8080")
$ErrorActionPreference = "Stop"

# Envoie une requête JSON à l'API et renvoie la réponse ; s'arrête en affichant l'erreur si l'appel échoue
function Send-Api($methode, $chemin, $corps) {
    $params = @{ Method = $methode; Uri = "$Url$chemin"; ContentType = "application/json; charset=utf-8" }
    if ($null -ne $corps) { $params.Body = [Text.Encoding]::UTF8.GetBytes(($corps | ConvertTo-Json -Depth 5)) }
    try { return Invoke-RestMethod @params }
    catch { Write-Host "ÉCHEC $methode $chemin : $($_.ErrorDetails.Message)"; throw }
}

# Date au format AAAA-MM-JJ, décalée de N mois par rapport à aujourd'hui
function Get-DateMois($mois) { (Get-Date).AddMonths($mois).ToString("yyyy-MM-dd") }

Write-Host "Clients..."
$clients = @{}
foreach ($c in @(
    @("Awa Koné", "0707112233", "Abobo Gare, Abidjan", 1000000),
    @("Ibrahim Traoré", "0505223344", "Adjamé Liberté, Abidjan", 800000),
    @("Fatou Bamba", "0101334455", "Quartier Commerce, Bouaké", 500000),
    @("Yao Kouassi", "0708445566", "Yopougon Niangon, Abidjan", 300000),
    @("Mariam Diallo", "0504556677", "Quartier Habitat, Yamoussoukro", 600000),
    @("Koffi N'Guessan", "0102667788", "Marché de Daloa", 0))) {
    $clients[$c[0]] = (Send-Api POST "/clients" @{ nom = $c[0]; telephone = $c[1]; adresse = $c[2]; plafondEmprunt = $c[3] }).id
}

Write-Host "Comptes..."
foreach ($c in @(
    @("CPT-0001", "EPARGNE", -24, 150000, "Awa Koné"),
    @("CPT-0002", "COURANT", -12, 45000, "Awa Koné"),
    @("CPT-0003", "EPARGNE", -18, 320000, "Ibrahim Traoré"),
    @("CPT-0004", "COURANT", -6, 12000, "Fatou Bamba"),
    @("CPT-0005", "EPARGNE", -3, 80000, "Yao Kouassi"),
    @("CPT-0006", "EPARGNE", -30, 510000, "Mariam Diallo"),
    @("CPT-0007", "COURANT", -2, 5000, "Mariam Diallo"))) {
    Send-Api POST "/comptes" @{ numero = $c[0]; type = $c[1]; dateOuverture = (Get-DateMois $c[2]); solde = $c[3]; clientId = $clients[$c[4]] } | Out-Null
}

# Crée un prêt, génère éventuellement son échéancier, puis paie les N premières échéances (99 = toutes)
function New-Pret($client, $montant, $duree, $taux, $moisAccord, $agent, $genererEcheancier, $echeancesPayees) {
    $pret = Send-Api POST "/prets" @{ montant = $montant; dureeMois = $duree; tauxAnnuel = $taux; dateAccord = (Get-DateMois $moisAccord); agent = $agent; clientId = $clients[$client] }
    if ($genererEcheancier) {
        $echeancier = Send-Api POST "/prets/$($pret.id)/echeancier" $null
        foreach ($e in ($echeancier.echeances | Select-Object -First $echeancesPayees)) {
            Send-Api POST "/paiements" @{ datePaiement = $e.dateEcheance; montant = $e.montantTotal; modeReglement = "MOBILE_MONEY"; pretId = $pret.id } | Out-Null
        }
    }
    return $pret.id
}

Write-Host "Prêts, échéanciers et paiements..."
$enRetard = New-Pret "Awa Koné" 300000 6 12 -5 "Yao Koffi" $true 2        # 2 échéances payées sur 4 échues : en retard
New-Pret "Awa Koné" 100000 3 10 -4 "Yao Koffi" $true 99 | Out-Null       # tout payé : passe SOLDE
New-Pret "Ibrahim Traoré" 500000 12 15 -1 "Bamba Seydou" $true 0 | Out-Null   # vient de démarrer
$fatou = New-Pret "Fatou Bamba" 200000 6 0 -3 "Bamba Seydou" $true 1   # taux nul, 1 échéance payée sur 3 : en retard
New-Pret "Mariam Diallo" 250000 10 18 -2 "Yao Koffi" $true 2 | Out-Null  # à jour
New-Pret "Yao Kouassi" 150000 6 12 0 "Bamba Seydou" $false 0 | Out-Null  # accordé, échéancier pas encore généré

Write-Host "Paiement partiel et pénalités de retard..."
Send-Api POST "/paiements" @{ datePaiement = (Get-DateMois 0); montant = 20000; modeReglement = "ESPECES"; pretId = $fatou } | Out-Null
Send-Api POST "/prets/$enRetard/penalites" $null | Out-Null

Write-Host "Détection des impayés (tâche nocturne lancée à la main)..."
$alertes = Send-Api POST "/alertes-impayes/execution" $null
Write-Host ("Terminé : {0} clients, 7 comptes, 6 prêts (dont 1 soldé, 2 en retard), {1} alerte(s) d'impayés." -f $clients.Count, @($alertes).Count)
