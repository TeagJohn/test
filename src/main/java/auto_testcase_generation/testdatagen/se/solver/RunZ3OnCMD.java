package auto_testcase_generation.testdatagen.se.solver;

import auto_testcase_generation.testdatagen.AbstractAutomatedTestdataGeneration;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Chạy file smt-lib trên cmd sử dụng SMT-Solver Z3
 *
 * @author anhanh
 */
public class RunZ3OnCMD {
    final static AkaLogger logger = AkaLogger.get(RunZ3OnCMD.class);

    private final String Z3Path;
    private final String smtLibPath;
    private String result = SpecialCharacter.EMPTY;

    public RunZ3OnCMD(String Z3Path, String smtLibPath) {
        this.Z3Path = Z3Path;
        this.smtLibPath = smtLibPath;
    }

    public synchronized void execute() throws Exception {
        logger.debug("RunZ3OnCMD begin");

        Date startTime = Calendar.getInstance().getTime();

        ProcessBuilder pb = null;

        if (Utils.isWindows()) {
            pb = new ProcessBuilder(new String[] { Utils.doubleNormalizePath(Z3Path), "-smt2", smtLibPath });
        } else if (Utils.isUnix()) {
            pb = new ProcessBuilder(new String[] { Z3Path, "-smt2", smtLibPath });
        } else if (Utils.isMac()) {
            pb = new ProcessBuilder(new String[] { Z3Path, "-smt2", smtLibPath });
        }

        File stdoutFile = new File("log.txt");
        File stderrFile = new File("error.txt");
        pb.redirectOutput(stdoutFile);
        pb.redirectError(stderrFile);

        assert pb != null;

        Process p = pb.start();
        p.waitFor(1, TimeUnit.MINUTES);
        // p.waitFor();

        AbstractAutomatedTestdataGeneration.numOfSolverCalls++;
        Date end = Calendar.getInstance().getTime();
        AbstractAutomatedTestdataGeneration.solverRunningTime += end.getTime() - startTime.getTime();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(stdoutFile)));
        String line = null;
        StringBuilder builder = new StringBuilder();
        while ((line = in.readLine()) != null) {
            builder.append(line).append(SpecialCharacter.LINE_BREAK);
        }
        result = builder.toString();
        in.close();

        // Display errors if exists
        BufferedReader err = new BufferedReader(new InputStreamReader(new FileInputStream(stderrFile)));
        String errLine = null;
        boolean hasError = false;
        while ((errLine = err.readLine()) != null) {
            logger.error(errLine);
            hasError = true;
        }
        if (hasError) {
            AbstractAutomatedTestdataGeneration.numOfSolverCallsbutCannotSolve++;
        }
        err.close();

        // Clear temp files
        stdoutFile.delete();
        stderrFile.delete();

        logger.debug("RunZ3OnCMD end");
    }

    public String getSolution() {
        return result;
    }
}
