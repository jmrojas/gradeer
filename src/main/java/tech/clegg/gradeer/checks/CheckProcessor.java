package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.auxiliaryprocesses.InspectionCommandProcess;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDExecutor;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckProcessor
{
    private Collection<Check> checks;
    private Configuration configuration;
    private Set<Solution> executedSolutions;

    private Set<Class<? extends Check>> presentCheckClasses;

    public CheckProcessor(Collection<Check> checks, Configuration configuration)
    {

        this.checks = checks;
        this.configuration = configuration;
        this.executedSolutions = new HashSet<>();
        this.presentCheckClasses = new HashSet<>();

        for (Check c : checks)
            presentCheckClasses.add(c.getClass());
    }

    public void runChecks(Solution solution)
    {
        // Run PMD on student solutions
        if(configuration.isPmdEnabled())
        {
            PMDExecutor pmdExecutor = new PMDExecutor(configuration);
            pmdExecutor.execute(solution);
        }

        // Run Checkstyle on student solutions if present
        runCheckstyle(solution);

        // Run inspection command (e.g. vscode)
        runInspectionCommand(solution);

        // Execute checks
        checks.forEach(c -> c.run(solution));
        executedSolutions.add(solution);
    }

    public boolean failsAllUnitTests(Solution solution)
    {
        // Only fails if TestSuiteChecks are present
        if(!presentCheckClasses.contains(TestSuiteCheck.class))
            return false;

        // Doesn't fail if any unit test passes
        for (Check c : checks)
        {
            if(c.getUnweightedScore(solution) > 0.0 && c.getClass().equals(TestSuiteCheck.class))
                return false;
        }

        return true;
    }

    public boolean wasExecuted(Solution solution)
    {
        return executedSolutions.contains(solution);
    }

    private void runCheckstyle(Solution solution)
    {
        if(presentCheckClasses.contains(CheckstyleCheck.class))
        {
            CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(configuration,
                    getChecks().stream()
                            .filter(c -> c instanceof CheckstyleCheck)
                            .map(c -> (CheckstyleCheck) c)
                            .collect(Collectors.toList()));
            checkstyleExecutor.execute(solution);
        }
    }

    private void runInspectionCommand(Solution solution)
    {
        if(configuration.getInspectionCommand() == null)
            return;
        if(configuration.getInspectionCommand().isEmpty())
            return;

        if(!presentCheckClasses.contains(ManualCheck.class))
            return;

        Collection<Path> toInspect = new ArrayList<>();

        // TODO find a more elegant solution for this
        // method inside corresponding classes?

        if(Files.exists(configuration.getTestOutputDir()))
            toInspect.add(Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier()));
        if(Files.exists(configuration.getMergedSolutionsDir()))
            toInspect.add(Paths.get(configuration.getMergedSolutionsDir() + File.separator + solution.getIdentifier() + ".java"));

        new InspectionCommandProcess(configuration, toInspect).run();
    }

    public Collection<Check> getChecks()
    {
        return checks;
    }
}
