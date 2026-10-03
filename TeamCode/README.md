# Code MantaBots 27318 : BIOBUZZ 2026-2027

Tout le code de l'équipe est dans `TeamCode/`. On ne modifie jamais `FtcRobotController/`, ni les
fichiers Gradle à la racine, ni le `README.md` de FIRST : ils sont remplacés à chaque mise à jour du SDK.

Librairies : [Pedro Pathing 3](https://pedropathing.com/docs/pathing) (trajectoires) et
[SolversLib](https://docs.seattlesolvers.com) (commandes et subsystems), déclarées dans `TeamCode/build.gradle`.

## Organisation

```
teamcode/
├── opmodes/
│   ├── teleop/MainTeleOp     le TeleOp de match
│   └── auto/MainAuto         un seul Auto, alliance choisie pendant l'init (X = bleu, B = rouge)
├── subsystems/               1 classe = 1 mécanisme (Drivetrain, Intake…)
├── robot/
│   ├── Robot                 crée tous les subsystems, active les bulk reads
│   └── HardwareNames         tous les noms de la config Driver Hub
├── pedro/
│   ├── Constants             réglages Pedro (sortie de l'AutoTune)
│   ├── Tuning                procédures AutoTune enregistrées
│   └── procedures/           copié du Quickstart Pedro, ne pas modifier
└── util/                     Alliance, LoopTimer…
```

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
| `frontLeft`   | Moteur            | Control Hub      | ?    |
| `frontRight`  | Moteur            | Control Hub      | ?    |
| `backLeft`    | Moteur            | Control Hub      | ?    |
| `backRight`   | Moteur            | Control Hub      | ?    |
| `pinpoint`    | goBILDA Pinpoint  | Control Hub I2C  | ?    |
| `intake`      | Moteur            | Expansion Hub    | ?    |

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
