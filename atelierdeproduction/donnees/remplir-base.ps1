# Remplit la base atelier_de_production avec un jeu de données de test, en passant par l'API (les règles métier s'appliquent).
# Les ordres sont placés sur la semaine en cours (plus un ordre en retard et un pour la semaine prochaine).
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

# Lundi de la semaine en cours, à minuit
$aujourdhui = (Get-Date).Date
$lundi = $aujourdhui.AddDays(-(([int]$aujourdhui.DayOfWeek + 6) % 7))
# Date et heure au format attendu par l'API, à partir du lundi : décalage en jours et heure de début
function Get-Creneau($jours, $heure) { $lundi.AddDays($jours).AddHours($heure).ToString("yyyy-MM-ddTHH:mm:ss") }

Write-Host "Articles..."
# référence, libellé, unité, type, temps unitaire (min), stock
$articles = @{}
foreach ($a in @(
    @("VELO", "Vélo de ville", "pièce", "FABRIQUE", 45, 0),
    @("VTT", "Vélo tout-terrain", "pièce", "FABRIQUE", 60, 0),
    @("ROUE", "Roue 26 pouces montée", "pièce", "FABRIQUE", 10, 0),
    @("MOYEU", "Moyeu assemblé", "pièce", "FABRIQUE", 5, 0),
    @("CADRE", "Cadre acier ville", "pièce", "ACHETE", 0, 30),
    @("CADRE_VTT", "Cadre aluminium VTT", "pièce", "ACHETE", 0, 10),
    @("RAYON", "Rayon inox", "pièce", "ACHETE", 0, 2000),
    @("JANTE", "Jante aluminium", "pièce", "ACHETE", 0, 100),
    @("PNEU", "Pneu 26 pouces", "pièce", "ACHETE", 0, 80),
    @("SELLE", "Selle confort", "pièce", "ACHETE", 0, 50),
    @("GUIDON", "Guidon acier", "pièce", "ACHETE", 0, 40),
    @("ROULEMENT", "Roulement à billes", "pièce", "ACHETE", 0, 200))) {
    $articles[$a[0]] = (Send-Api POST "/articles" @{ reference = $a[0]; libelle = $a[1]; unite = $a[2]; type = $a[3]; tempsUnitaireMinutes = $a[4]; stock = $a[5] }).id
}

Write-Host "Nomenclatures..."
foreach ($l in @(
    @("VELO", "ROUE", 2), @("VELO", "CADRE", 1), @("VELO", "SELLE", 1), @("VELO", "GUIDON", 1),
    @("VTT", "ROUE", 2), @("VTT", "CADRE_VTT", 1), @("VTT", "SELLE", 1), @("VTT", "GUIDON", 1),
    @("ROUE", "RAYON", 32), @("ROUE", "JANTE", 1), @("ROUE", "PNEU", 1), @("ROUE", "MOYEU", 1),
    @("MOYEU", "ROULEMENT", 2))) {
    Send-Api POST "/nomenclatures" @{ composeId = $articles[$l[0]]; composantId = $articles[$l[1]]; quantite = $l[2] } | Out-Null
}

Write-Host "Versions de nomenclature..."
foreach ($ref in "VELO", "VTT", "ROUE") {
    Send-Api POST "/articles/$($articles[$ref])/nomenclature/version" $null | Out-Null
    Send-Api POST "/articles/$($articles[$ref])/nomenclature/version/application" $null | Out-Null
}

Write-Host "Machines et opérateurs..."
$machines = @{}
foreach ($m in @(@("M1", "Banc de montage", "Assemblage"), @("M2", "Rayonneuse", "Roues"), @("M3", "Presse à moyeux", "Roues"))) {
    $machines[$m[0]] = (Send-Api POST "/machines" @{ code = $m[0]; type = $m[1]; atelier = $m[2] }).id
}
$operateurs = @{}
foreach ($o in @(@("Kouamé Yao", "OP001", "Matin"), @("Aya Konaté", "OP002", "Matin"), @("Serge Gbagbo", "OP003", "Après-midi"))) {
    $operateurs[$o[1]] = (Send-Api POST "/operateurs" @{ nom = $o[0]; matricule = $o[1]; equipe = $o[2] }).id
}

Write-Host "Maintenance..."
Send-Api POST "/machines/$($machines['M1'])/maintenances" @{ debut = (Get-Creneau 2 8); fin = (Get-Creneau 2 12); motif = "Révision trimestrielle" } | Out-Null

Write-Host "Ordres de fabrication et statuts..."
# article, quantité, (jours après lundi, heure), machine, opérateur, statuts successifs
foreach ($o in @(
    @("ROUE", 20, 0, 8, "M2", "OP002", @("LANCE", "TERMINE")),
    @("VELO", 6, 0, 8, "M1", "OP001", @("LANCE")),
    @("VTT", 4, 1, 8, "M1", "OP001", @()),
    @("ROUE", 16, 2, 8, "M2", "OP002", @()),
    @("MOYEU", 40, 2, 9, "M3", "OP003", @()),
    @("VELO", 5, -14, 8, "M1", "OP001", @()),
    @("VTT", 10, 3, 8, "M1", "OP003", @("ANNULE")),
    @("VELO", 100, 7, 8, "M1", "OP001", @()))) {
    $ordre = Send-Api POST "/ordres-fabrication" @{ articleId = $articles[$o[0]]; quantite = $o[1]; datePrevue = (Get-Creneau $o[2] $o[3]); machineId = $machines[$o[4]]; operateurId = $operateurs[$o[5]] }
    foreach ($statut in $o[6]) { Send-Api POST "/ordres-fabrication/$($ordre.id)/statut" @{ statut = $statut } | Out-Null }
}

Write-Host "Ordres en retard (tâche nocturne lancée à la main)..."
$alertes = Send-Api POST "/alertes-retard/execution" $null
Write-Host ("Terminé : {0} articles, 13 lignes de nomenclature, 3 versions, {1} machines, {2} opérateurs, 8 ordres, {3} alerte(s) de retard." -f `
    $articles.Count, $machines.Count, $operateurs.Count, @($alertes).Count)
