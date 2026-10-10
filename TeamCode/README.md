# Code MantaBots 27318 : BIOBUZZ 2026-2027

Tout le code de l'équipe est dans `TeamCode/`. On ne modifie jamais `FtcRobotController/`, ni les
fichiers Gradle à la racine, ni le `README.md` de FIRST : ils sont remplacés à chaque mise à jour du SDK.

Librairies : [Pedro Pathing 3](https://pedropathing.com/docs/pathing) (trajectoires) et
[SolversLib](https://docs.seattlesolvers.com) (commandes et subsystems), déclarées dans `TeamCode/build.gradle`.

Ce qu'on a appris des meilleures équipes, et la liste des priorités : [docs/recherche-top-equipes.md](docs/recherche-top-equipes.md).

## Organisation

```
teamcode/
├── opmodes/
│   ├── teleop/MainTeleOp     « A. TeleOp », le TeleOp de match
│   ├── auto/MainAuto         « A. Auto », un seul Auto, alliance choisie pendant l'init (X = bleu, B = rouge)
│   └── test/                 SystemCheck (check d'avant-match), DriveTest (rouler sans Pedro), SlothTest…
├── subsystems/               1 classe = 1 mécanisme (Drivetrain, Intake…)
├── robot/
│   ├── Robot                 crée tous les subsystems, active les bulk reads
│   ├── BatteryVoltage        tension de la batterie (relue toutes les 500 ms) et compensation
│   └── HardwareNames         tous les noms de la config Driver Hub
├── pedro/
│   ├── Constants             réglages Pedro (sortie de l'AutoTune)
│   ├── Tuning                procédures AutoTune enregistrées
│   └── procedures/           copié du Quickstart Pedro, ne pas modifier
└── util/                     Alliance, MatchState, LoopTimer…
```

Le script `scripts/update-pedro-procedures.sh` remplace `pedro/procedures/` par la dernière version du
Quickstart Pedro et note la révision dans `pedro/procedures/QUICKSTART_REV`. À lancer quand Pedro sort une
mise à jour, puis faire un commit à part.

## Pendant un match

1. **Auto** (« A. Auto ») : choisir l'alliance pendant l'init (X = bleu, B = rouge). L'Auto enregistre à
   chaque boucle sa position et son alliance (`MatchState`). Chaque trajectoire a un temps max
   (`PATH_TIMEOUT_MS`), et au-delà de `SCORING_TIMEOUT_MS` le robot abandonne et va se garer.
2. **TeleOp** (« A. TeleOp », présélectionné à la fin de l'Auto) : il reprend la position et l'alliance de
   l'Auto si celui-ci a tourné il y a moins de 3 minutes. Sinon, placer le robot dos au pilote et choisir
   l'alliance pendant l'init. BACK (manette 1) recale le cap quand le robot est dos au pilote.
3. Après un rechargement Sloth ou un redémarrage de l'app, `MatchState` est vide : le TeleOp repart du
   cas « robot dos au pilote ».

Réglage à faire une fois sur le terrain : `Drivetrain.RED_DRIVER_FORWARD_DEG`, le cap Pedro d'un robot
qui s'éloigne du pilote rouge (modifiable en direct dans Panels, puis à recopier dans le code).

**Dans les pits** : lancer « System Check » (groupe Test) pour faire tourner chaque moteur et servo un par un
et vérifier qu'il est bien branché.

## Règles

- **Un OpMode ne touche jamais au `hardwareMap`.** Il crée un `Robot` et appelle les subsystems.
- **Chaque subsystem a une API de haut niveau** (`intake.collect()`, pas `motor.set(1.0)` dans un OpMode).
  Les réglages (`public static`) sont en haut de la classe, avec l'unité dans le nom si besoin.
- **Rien de bloquant dans la boucle** : pas de `sleep()`, pas de `while` qui attend. On utilise des commandes
  (`WaitCommand`, `SequentialCommandGroup`, `StartEndCommand`…).
- **`follower.update()` n'est appelé qu'à un seul endroit** : `Drivetrain.periodic()`.
- **On surveille la fréquence de boucle** (télémétrie « Boucle »). Si elle chute après un changement, c'est ce changement.
- **Nommage Java** : `PascalCase` pour les classes, `camelCase` pour les méthodes, `UPPER_SNAKE` pour les constantes.
- Les vieux OpModes de test passent en `@Disabled` ou sont supprimés. Git garde l'historique, donc pas de code commenté.

## Câblage

À tenir à jour à chaque changement de config sur le Driver Hub.

| Nom (config)  | Type              | Hub              | Port |
|---------------|-------------------|------------------|------|
| `frontLeft`   | Moteur            | Control Hub      | 0    |
| `backLeft`    | Moteur            | Control Hub      | 1    |
| `backRight`   | Moteur            | Control Hub      | 2    |
| `frontRight`  | Moteur            | Control Hub      | 3    |
| `pinpoint`    | goBILDA Pinpoint  | Control Hub I2C  | ?    |
| `intake`      | Moteur            | Expansion Hub    | ?    |
| `bar`         | Servo             | Control Hub      | 5    |

## Responsables

Chaque mécanisme a une personne référente : elle connaît son code, relit les PR qui le touchent et
sait le réparer en compétition. Tout le monde peut modifier tout le code, mais avec sa relecture.

| Partie                         | Responsable | Remplaçant·e |
|--------------------------------|-------------|--------------|
| Drivetrain + réglage Pedro     |             |              |
| Intake                         |             |              |
| Auto (trajectoires)            |             |              |
| CI / Git / mises à jour du SDK |             |              |

## Réglage Pedro (dès que la base roule)

1. Créer la config sur le Driver Hub avec les noms de `HardwareNames`.
2. Lancer l'AutoTune dans cet ordre : **Mecanum Tuner, puis Pinpoint Tuner, puis Foresight Tuner, puis Tests**
   (voir la [doc Pedro](https://pedropathing.com/docs/pathing/tuning)).
3. Coller le code généré par chaque tuner dans `pedro/Constants.java` (aux endroits marqués `TODO`).

Tant que le Foresight Tuner n'a pas été fait, le TeleOp et l'Auto refusent de démarrer avec un message explicite.

## Panels et Sloth

- **Panels** (réglage en direct et télémétrie) : se connecter au Wi-Fi du robot et ouvrir
  `http://192.168.43.1:8001` dans le navigateur. Panels n'affiche que la télémétrie qu'on lui envoie : chaque
  OpMode commence son init par `telemetry = TelemetryUtil.withPanels(telemetry);`, qui envoie chaque ligne au
  Driver Hub et à Panels. Les valeurs changées dans Panels sont perdues au prochain déploiement : recopier
  dans le code celles qu'on garde.
- **Sloth** (rechargement à chaud) : après une installation complète, la tâche `deploySloth` (VS Code :
  **FTC: Hot Reload (Sloth)**) envoie seulement le code `teamcode` en une ou deux secondes. Attendre la fin
  du chargement avant d'appuyer sur INIT.
- **Installation complète obligatoire** après un changement de librairie ou de `build.gradle`, de
  `FtcRobotController/`, du manifeste ou de `res/` (config matérielle comprise), et avant chaque compétition.
- Si un ancien code revient sans cesse : `adb shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*`.
- Les versions de Sloth, du plugin Load et le préfixe de Panels (`0.3.2+…`) doivent rester identiques.

## Workflow Git

1. Créer une branche : `git switch -c feat/nom-du-truc`
2. Faire des commits petits, avec des messages clairs (`Add lift PID`, pas `fix`)
3. Pousser, puis ouvrir une Pull Request vers `master`
4. Fusionner seulement quand le check « Build » est vert et qu'une autre personne a relu
5. Avant chaque compétition : `git tag nom-de-la-competition`, puis `git push --tags`

En compétition, on ne déploie que du code committé, pour toujours savoir ce qui tourne sur le robot.

### Mettre à jour le SDK FIRST

```bash
git fetch upstream
git merge upstream/master
```

### Mettre à jour Pedro ou SolversLib

Changer les versions dans `TeamCode/build.gradle`. Pour Pedro, recopier aussi `pedro/procedures/`
depuis le [Quickstart](https://github.com/Pedro-Pathing/Quickstart).
