# Remplit la base place_de_marche avec un jeu de données de test, en passant par l'API (les règles métier s'appliquent).
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

Write-Host "Catégories..."
$categories = @{}
foreach ($c in @(
    @("Électronique", "Téléphones, ordinateurs et accessoires"),
    @("Mode", "Vêtements, chaussures et pagnes"),
    @("Maison", "Équipement et décoration de la maison"),
    @("Alimentation", "Produits du terroir et épicerie fine"))) {
    $categories[$c[0]] = (Send-Api POST "/categories" @{ libelle = $c[0]; description = $c[1] }).id
}

Write-Host "Vendeurs..."
$vendeurs = @{}
foreach ($v in @(
    @("Tech Abidjan", "contact@techabidjan.ci", "Abidjan"),
    @("Mode Ivoire", "ventes@modeivoire.ci", "Bouaké"),
    @("Maison Plus", "info@maisonplus.ci", "Yamoussoukro"),
    @("Saveurs d'Afrique", "commandes@saveurs.ci", "San-Pédro"))) {
    $vendeurs[$v[0]] = (Send-Api POST "/vendeurs" @{ raisonSociale = $v[0]; email = $v[1]; ville = $v[2] }).id
}

Write-Host "Produits..."
# nom, description, prix, stock, vendeur, catégorie (stocks variés : normaux, faibles <= 5, et un en rupture)
$produits = @{}
foreach ($p in @(
    @("Smartphone Galaxy A15", "Écran 6,5 pouces, 128 Go", 135000, 25, "Tech Abidjan", "Électronique"),
    @("Casque Bluetooth", "Réduction de bruit, 30 h d'autonomie", 25000, 4, "Tech Abidjan", "Électronique"),
    @("Ordinateur portable 15 pouces", "Core i5, 8 Go, SSD 512 Go", 420000, 6, "Tech Abidjan", "Électronique"),
    @("Clé USB 64 Go", "USB 3.0", 6500, 0, "Tech Abidjan", "Électronique"),
    @("Pagne wax 6 yards", "Coton imprimé", 18000, 40, "Mode Ivoire", "Mode"),
    @("Chemise en bogolan", "Taille M à XL", 22000, 3, "Mode Ivoire", "Mode"),
    @("Sandales en cuir", "Fabrication artisanale", 15000, 12, "Mode Ivoire", "Mode"),
    @("Ventilateur sur pied", "3 vitesses, 45 W", 28000, 9, "Maison Plus", "Maison"),
    @("Marmite en fonte 10 L", "Pour plats familiaux", 35000, 2, "Maison Plus", "Maison"),
    @("Natte tressée", "2 m x 1,5 m", 9000, 20, "Maison Plus", "Maison"),
    @("Attiéké séché 1 kg", "Production de Dabou", 2500, 60, "Saveurs d'Afrique", "Alimentation"),
    @("Café de Man 500 g", "Arabica torréfié", 7500, 5, "Saveurs d'Afrique", "Alimentation"))) {
    $produits[$p[0]] = (Send-Api POST "/produits" @{ nom = $p[0]; description = $p[1]; prix = $p[2]; stock = $p[3]; vendeurId = $vendeurs[$p[4]]; categorieId = $categories[$p[5]] }).id
}

Write-Host "Clients..."
$clients = @{}
foreach ($c in @(
    @("Awa Koné", "awa.kone@mail.ci", "Cocody Riviera 2, Abidjan"),
    @("Ibrahim Traoré", "ibrahim.traore@mail.ci", "Plateau, Abidjan"),
    @("Fatou Bamba", "fatou.bamba@mail.ci", "Quartier Commerce, Bouaké"),
    @("Yao Kouassi", "yao.kouassi@mail.ci", "Yopougon Siporex, Abidjan"),
    @("Mariam Diallo", "mariam.diallo@mail.ci", "Quartier Habitat, Yamoussoukro"))) {
    $clients[$c[0]] = (Send-Api POST "/clients" @{ nom = $c[0]; email = $c[1]; adresseLivraison = $c[2] }).id
}

Write-Host "Commandes et suivi des statuts..."
# client, produit, quantité, statuts successifs à appliquer ("ANNULATION" = POST /annulation)
foreach ($c in @(
    @("Awa Koné", "Smartphone Galaxy A15", 1, @("PAYEE", "EXPEDIEE", "LIVREE")),
    @("Awa Koné", "Pagne wax 6 yards", 3, @("PAYEE", "EXPEDIEE", "LIVREE")),
    @("Ibrahim Traoré", "Ordinateur portable 15 pouces", 1, @("PAYEE", "EXPEDIEE", "LIVREE")),
    @("Ibrahim Traoré", "Casque Bluetooth", 1, @("PAYEE")),
    @("Fatou Bamba", "Sandales en cuir", 2, @("PAYEE")),
    @("Fatou Bamba", "Attiéké séché 1 kg", 10, @("PAYEE", "EXPEDIEE")),
    @("Yao Kouassi", "Ventilateur sur pied", 1, @()),
    @("Yao Kouassi", "Smartphone Galaxy A15", 2, @("ANNULATION")),
    @("Mariam Diallo", "Marmite en fonte 10 L", 1, @("PAYEE")),
    @("Mariam Diallo", "Café de Man 500 g", 2, @()),
    @("Awa Koné", "Natte tressée", 2, @("PAYEE", "EXPEDIEE", "LIVREE")),
    @("Yao Kouassi", "Chemise en bogolan", 1, @("PAYEE", "ANNULATION")))) {
    $commande = Send-Api POST "/commandes" @{ clientId = $clients[$c[0]]; produitId = $produits[$c[1]]; quantite = $c[2] }
    foreach ($statut in $c[3]) {
        if ($statut -eq "ANNULATION") { Send-Api POST "/commandes/$($commande.id)/annulation" $null | Out-Null }
        else { Send-Api POST "/commandes/$($commande.id)/statut" @{ statut = $statut } | Out-Null }
    }
}

Write-Host "Alertes de stock faible (tâche nocturne lancée à la main)..."
$alertes = Send-Api POST "/alertes-stock/execution" $null
Write-Host ("Terminé : {0} catégories, {1} vendeurs, {2} produits, {3} clients, 12 commandes, {4} alerte(s) de stock." -f `
    $categories.Count, $vendeurs.Count, $produits.Count, $clients.Count, @($alertes).Count)
