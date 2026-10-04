# Ce que font les meilleures équipes FTC

Recherche faite en octobre 2026 sur 36 repos publics de 28 équipes : champions du monde 2023 à 2026,
meilleurs OPR mondiaux (FTCScout) et équipes connues pour leur code. Le but : savoir ce qui vaut la
peine d'être copié dans notre code, et ce qui ne l'est pas.

## Ce qu'on avait déjà la saison dernière

- **Panels** pour le réglage en direct et la télémétrie (librairie de Lazar / FTControl, distribuée
  par le dépôt Maven de Dairy). À remettre cette saison.
- **La config matérielle versionnée** dans `TeamCode/src/main/res/xml/`. À refaire pour BIOBUZZ.
- Pas de **Sloth** (rechargement à chaud), voir la section dédiée plus bas.

## Équipes étudiées

| Équipe | Palmarès | À retenir |
|---|---|---|
| 11212 The Clueless | Champions du monde 2024 | Framework 100 % maison, écritures moteurs par priorité avec budget de temps par boucle, journal CSV de chaque match |
| 21229 Quality Control | Champions du monde 2023 | Matériel factice si un appareil manque, position et heure de fin d'Auto sauvegardées dans un fichier, classe `AutoBase` |
| 19066 AiCitizens | Champions du monde 2024 | Moteur de commandes et suivi de trajectoire maison (Kotlin), IMU lue dans un thread |
| 11228 OverClucked Bots | Champions du monde 2026 | Code simple, mais Autos avec plan B (« failover ») et mode manuel de secours |
| 14481 Don't Blink | Champions du monde 2023 | Déjà en BIOBUZZ avec Pedro 3.0.1 et tuning 1.0.1, les mêmes versions que nous |
| 16379 KookyBotz | Top OPR 2024, code très suivi | Cycle `read()` / `periodic()` / `write()` pour chaque subsystem, commandes rangées par usage |
| 12808 RevAmped | 4e OPR mondial 2026 | Menus de sélection pendant l'init, test automatique du robot, simulateur Pedro |
| 19043 CyLiis | Top 5 OPR | Noms de config par port (`ch0`, `eh1`…), servos qui estiment quand ils sont arrivés |
| 11329 ICE | Vainqueurs de division aux Mondiaux 2026 | Autos modulaires (chaque module démarre là où le précédent finit), README avec photo |
| 23511 Seattle Solvers | Auteurs de SolversLib | Tests JUnit sur les calculs lancés par la CI, profileur de boucle en CSV, guides de réglage en .md |
| 19411 Tech Tigers | Forte culture logicielle | 124 PR, CI avec build + tests + lint, OpMode de diagnostic pour les pits |
| 6165 MSET Cuttlefish | Déjà avancés en BIOBUZZ | Code réutilisable (`architecture/`) séparé du code de saison, système de « faults », guide de conventions |

## Ce que font presque tous les meilleurs

1. **Réglage en direct** avec FTC Dashboard ou Panels (`@Config` / `@Configurable`) : présent dans
   tous les repos de robot étudiés.
2. **Boucle optimisée** :
   - bulk reads et cache d'écriture des moteurs (on les a déjà) ;
   - compensation de tension (`puissance × 12 V / tension mesurée`), la tension étant relue
     seulement toutes les 0,2 à 5 s car la lire coûte cher ;
   - capteurs I2C lents (couleur, distance, IMU) lus moins souvent ou dans un thread.
3. **Fin d'Auto transmise au TeleOp** : position et alliance, parfois sauvegardées dans un fichier
   pour survivre à un redémarrage de l'app.
4. **Un OpMode de test par mécanisme**, plus un check d'avant-match qu'on fait défiler à la manette.
5. **Machines à états en `enum`** dans chaque subsystem.
6. **Mesure du courant moteur** (25 repos) : détecter une pièce prise, un blocage, un risque de brownout.
7. **Config matérielle versionnée** dans `res/xml/` (Seattle Solvers, RevAmped, MSET, Tech Tigers,
   Escape Velocity, Clueless).

## Révélations

- **Gagner les Mondiaux ne demande pas un code propre.** OverClucked (champions 2026) et Clueless
  (champions 2024) travaillent sur une seule branche, avec du code commenté et des OpModes
  rouge/bleu dupliqués. Ce qui fait les points : le robot et l'entraînement des pilotes. La qualité
  du code joue sur la fiabilité et la vitesse à laquelle on itère.
- **PR et CI sont rares.** Seules les équipes à forte culture logicielle s'en servent (Tech Tigers,
  Escape Velocity, KookyBotz, Seattle Solvers). Notre setup nous place dans ce groupe. Il faut juste
  rester souple : pas de relecture obligatoire en pleine compétition.
- **Tests unitaires :** une seule équipe (Seattle Solvers), uniquement pour les calculs.
- **Rouge/bleu :** la majorité duplique les OpModes. Les meilleurs paramètrent avec une
  transformation de pose, comme nous.
- **Le terrain BIOBUZZ est symétrique par rotation de 180°**, pas en miroir (manuel, section 9 :
  GARDENS dans des coins opposés, AprilTags de la cellule rouge côté fond et de la bleue côté
  public). Corrigé dans `util/Alliance.java`.
- **Sloth est déjà dans notre APK** : Pedro tuning 1.0.1 l'embarque (version 0.3.2). Il ne manque
  que le plugin Gradle pour s'en servir.

## Priorités pour notre code

### Priorité 1 : avant le premier robot

- [x] Corriger la symétrie rouge/bleu dans `Alliance.java` (rotation de 180°)
- [x] Remettre **Panels** (version compatible Sloth)
- [x] Classe `MatchState` : position et alliance de fin d'Auto, relues au démarrage du TeleOp
- [x] Lecture périodique de la tension et compensation des puissances
- [x] Timeout global sur l'Auto (et un temps max par trajectoire)
- [x] OpMode **SystemCheck** qui fait tourner chaque moteur et servo un par un
- [ ] Config matérielle BIOBUZZ dans `res/xml/` (en attente du câblage du robot)
- [x] Noms d'OpModes préfixés (« A. TeleOp ») pour qu'ils sortent en tête sur le Driver Hub

### Priorité 2 : pour gagner du temps

- [x] **Sloth** pour recharger le code en moins d'une seconde (voir plus bas)
- [x] Script qui met à jour `pedro/procedures/` depuis le Quickstart, avec la révision épinglée
      dans un fichier (comme MSET)
- [ ] Télémétrie Driver Hub construite seulement quand elle est réellement envoyée (toutes les 250 ms)
- [ ] Journal CSV de chaque match

### Priorité 3 : quand le robot existe

- [ ] Menus de sélection pendant l'init : alliance, délai, variante d'Auto
- [ ] Autos modulaires (modèle ICE)
- [ ] Tests JUnit pour les calculs, lancés par la CI
- [ ] « Faults » affichés en gros sur le Driver Hub quand un capteur lâche

### À ne pas copier

- Les singletons et le matériel en `static` partout.
- Le code commenté et les dossiers `old/` : git garde l'historique.
- PhotonCore : le gain est réel mais c'est expérimental. Seulement si la boucle devient un problème.

## Sloth en détail

**Ce que c'est.** Une librairie de la Dairy Foundation qui envoie seulement notre code
(`org.firstinspires.ftc.teamcode`) au robot, au lieu de réinstaller toute l'app. Moins de 2 s au lieu
de 40 s ou plus. Les changements survivent aux redémarrages du robot, et le nouveau code n'est
chargé qu'à la fin de l'OpMode en cours, donc on peut déployer pendant qu'un OpMode tourne.

**Pourquoi c'est utile pour nous.** Les séances de réglage (PID, positions de servo, trajectoires)
consistent à modifier une valeur, déployer, tester, recommencer. Diviser l'attente par 20 change la
quantité de tests faisables dans une séance.

**Ce qu'il faut ajouter** dans `TeamCode/build.gradle` (la librairie Sloth 0.3.2 est déjà là via
Pedro tuning) :

```groovy
buildscript {
    repositories {
        mavenCentral()
        google()
        maven { url "https://repo.dairy.foundation/releases" }
    }
    dependencies {
        classpath "dev.frozenmilk:Load:0.3.2"
    }
}

apply plugin: 'dev.frozenmilk.sinister.sloth.load'
```

Pour Panels, prendre la version compatible : `com.bylazar.sloth:fullpanels:0.3.2+1.0.13`.
Pour FTC Dashboard : `com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0`. La version de Sloth et
celle du préfixe doivent rester identiques.

**Utilisation.**
- `./gradlew :TeamCode:deploySloth` (ou une configuration Gradle `deploySloth` dans Android Studio)
  pour les changements de code ordinaires.
- Installation complète normale (avec la tâche `removeSlothRemote` en premier) pour : la première
  fois, un changement de librairie ou de `build.gradle`, un changement dans `FtcRobotController/`,
  dans le manifeste ou dans `res/`.

**Pièges connus.**
- Un code qui compile peut ne pas marcher après rechargement si on a changé une librairie ou un
  fichier hors de `teamcode` : dans ce cas, installation complète.
- Si un vieux code Sloth revient sans cesse remplacer le nouveau : supprimer
  `/storage/emulated/0/FIRST/dairy/sloth/` sur le robot
  (`adb shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*`). Problème fréquent cette saison
  chez les équipes qui passent à Pedro 3.
- Attendre la fin du chargement (écran du Control Hub ou journal du Driver Hub) avant de redéployer
  ou d'appuyer sur INIT. MSET signale qu'un INIT pendant un chargement peut redémarrer l'app.
- Les valeurs changées dans Panels / Dashboard sont perdues au rechargement : recopier dans le
  code celles qu'on garde.
- **En compétition** : faire une installation complète avant les matchs et ne plus utiliser Sloth,
  pour savoir exactement quel code tourne.

Qui l'utilise : MSET Cuttlefish, ICE, Don't Blink (avec slothboard), le kit de démarrage BrainSTEM.
RoboKings et Escape Velocity l'avaient installé puis désactivé.

## Pièges Pedro 3.0.1 relevés par MSET

- Le tuning AutoTune se fait sur `http://192.168.43.1:10158` (connecté au Wi-Fi du robot).
  Ordre : Mecanum, Pinpoint, Foresight, puis Tests (laisser une zone libre de 48 × 48 pouces).
- Garder le robot immobile pendant la première seconde de l'init : le Pinpoint calibre son IMU.
- Une méthode `@Tuner` doit être `static`, sans argument, et retourner exactement `Procedure`,
  sinon l'app plante au démarrage.
- Un chemin doit avoir une interpolation de cap (par exemple `.linear(a, b)`), sinon Pedro lève une
  exception.
- `length()` d'une courbe peut sous-estimer la longueur des courbes en S : ne pas s'en servir pour
  des timeouts.
- `Interpolator.piecewise()` a des défauts en 3.0.1 : préférer une interpolation simple par segment.

## Sources

- Résultats des Mondiaux : [2023](https://ftc-events.firstinspires.org/2022/FTCCMP1/awards),
  [2024](https://ftc-events.firstinspires.org/2023/FTCCMP1/awards),
  [2025](https://ftc-events.firstinspires.org/2024/FTCCMP1/awards),
  [2026](https://ftc-events.firstinspires.org/2025/FTCCMP1/awards)
- [Manuel BIOBUZZ, section 9 (Arena)](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-09)
- [Sloth](https://github.com/Dairy-Foundation/Sloth)
- Repos : [Clueless](https://github.com/FTCclueless/Decode),
  [Quality Control](https://github.com/21229QualityControl/CenterstageV2),
  [AiCitizens](https://github.com/petrustoica/CenterStage),
  [OverClucked](https://github.com/cmcarpen85/OCB_Decode2025_26),
  [Don't Blink](https://github.com/DontBlink14481/Biobuzz14481),
  [KookyBotz](https://github.com/KookyBotz/CenterStage),
  [RevAmped](https://github.com/junkjunk123/RevAmped-Decode-V2),
  [CyLiis](https://github.com/Cyliis/CyLiis_Into_The_Deep_OfficialCode),
  [ICE](https://github.com/FTC11329/11329-2026-repo),
  [Seattle Solvers](https://github.com/FTC-23511/Decode-2026),
  [Tech Tigers](https://github.com/techtigers-ftc/decode),
  [MSET Cuttlefish](https://github.com/6165-MSET-Cuttlefish/BioBuzz)
