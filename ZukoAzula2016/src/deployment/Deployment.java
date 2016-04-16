package deployment;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import ccre.deployment.eggs.ArtifactDeployer;
import ccre.deployment.eggs.DepEgg;
import ccre.frc.FRCApplication;

/**
 * The Deployment class of your project. When your project is built, the static
 * methods in this class that are annotated with <code>@DepTask</code> will be
 * available as options in the Eclipse External Tools menu.
 */
public class Deployment {

    /**
     * The reference to your main class. When you change which class is the main
     * class of your project, make sure to change this line.
     */
    public static final Class<? extends FRCApplication> robotMain = org.team1540.zukoazula.ZukoAzula.class;

    public static final Class<?> kangarooMain = org.team1540.kangaroo.KangarooMain.class;

    /**
     * A deployment task that downloads your robot code to a roboRIO found based
     * on your team number.
     *
     * @throws Exception
     */
    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);

        try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
            rshell.archiveLogsTo(DepProject.root());

            rshell.downloadAndStart(result);
        }
    }

    /**
     * A deployment task that runs your robot code in the CCRE's emulator.
     *
     * @throws Exception
     */
    @DepTask(fork = true)
    public static void emulate() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);
        DepEmulator.emulate(result);
    }

    /**
     * A deployment task that packages a snapshot of your code such that it can
     * be deployed to the robot by anyone, even if they don't have the CCRE set
     * up.
     *
     * @throws Exception
     */
    @DepTask
    public static void layEgg() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);
        DepEgg.layEgg(result, new ArtifactDeployer() {
            @Override
            public void deployArtifact(Artifact artifact) throws Exception {
                try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
                    rshell.downloadAndStart(artifact);
                }
            }
        });
    }

    @DepTask
    public static void deployKangaroo() throws Exception {
        Artifact result = DepKangaroo.buildProject(kangarooMain);

        try (DepKangaroo.KangarooShell shell = DepKangaroo.connectAndVerify("10.15.40.14")) {
            shell.archiveLogsTo(DepProject.root());
            shell.downloadAndStart(result);
        }
    }
}
