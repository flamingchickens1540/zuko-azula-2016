package deployment;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepJar;
import ccre.deployment.DepJava;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepRoboRIO.RIOShell;
import ccre.deployment.DepTask;
import ccre.deployment.JarBuilder;
import ccre.deployment.eggs.ArtifactDeployer;
import ccre.deployment.eggs.DepEgg;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.net.Network;

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

    private static Artifact build() throws IOException {
        Artifact viewko = DepJava.build(DepProject.directory("../Viewko/src"), new File(DepProject.ccreProject("CommonChickenRuntimeEngine"), "CCRE.jar"));
        Artifact newcode = DepJava.build(DepProject.directory("src"), DepRoboRIO.getJarFile(DepRoboRIO.LIBS_THICK), viewko.toJar(false).toFile());
        return DepJar.combine(DepRoboRIO.manifest(robotMain), JarBuilder.DELETE, newcode, DepRoboRIO.getJar(DepRoboRIO.LIBS_THIN), viewko);
    }

    /**
     * A deployment task that downloads your robot code to a roboRIO found based
     * on your team number.
     *
     * @throws Exception
     */
    @DepTask
    public static void deploy() throws Exception {
        Artifact result = build();

        try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
            rshell.archiveLogsTo(DepProject.root());

            rshell.downloadAndStart(result);
        }
    }

    @DepTask
    public static void deployCompetition() throws Exception {
        Logger.info("Scanning...");
        boolean found = false;
        for (String addr : Network.listIPv4Addresses()) {
            if (addr.startsWith("10.15.40.")) {
                found = true;
                break;
            } else if (!addr.startsWith("127.0.0")) {
                Logger.warning("Found extraneous address: " + addr);
            }
        }
        if (!found) {
            Logger.info("No valid local address! Likely to fail.");
        }
        Artifact result = build();
        DepRoboRIO rio = DepRoboRIO.byNameOrIP("10.15.40.19");
        if (rio == null) {
            Logger.severe("Could not connect to 10.15.40.19!");
            return;
        }
        try (DepRoboRIO.RIOShell rshell = rio.openDefaultShell()) {
            rshell.verifyRIO();
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
        Artifact result = build();
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
        Artifact result = build();
        DepEgg.layEgg(result, new ArtifactDeployer() {
            @Override
            public void deployArtifact(Artifact artifact) throws Exception {
                try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
                    rshell.downloadAndStart(artifact);
                }
            }
        });
    }
}
