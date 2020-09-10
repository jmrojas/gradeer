package tech.clegg.gradeer.solution;

import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleProcessResults;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDProcessResults;
import tech.clegg.gradeer.subject.JavaSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Solution
{
    private static Logger logger = LogManager.getLogger(Solution.class);

    Path directory;
    Collection<JavaSource> sources;

    private CheckstyleProcessResults checkstyleProcessResults;
    private PMDProcessResults pmdProcessResults;

    private Collection<Flag> flags = new HashSet<>();

    public Solution(Path locationDir)
    {
        this.directory = locationDir;
        try
        {
            sources = Files.walk(directory)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    .filter(p -> !p.toString().contains("__MACOSX")) // Remove the hidden files generated by OSX, can't be parsed correctly.
                    .map(p -> new JavaSource(p, locationDir))
                    .collect(Collectors.toList());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setCheckstyleProcessResults(CheckstyleProcessResults checkstyleProcessResults)
    {
        this.checkstyleProcessResults = checkstyleProcessResults;
    }

    public void setPmdProcessResults(PMDProcessResults pmdProcessResults)
    {
        this.pmdProcessResults = pmdProcessResults;
    }

    public CheckstyleProcessResults getCheckstyleProcessResults()
    {
        return checkstyleProcessResults;
    }

    public PMDProcessResults getPmdProcessResults()
    {
        return pmdProcessResults;
    }

    public Path getDirectory()
    {
        return directory;
    }

    public Collection<JavaSource> getSources()
    {
        return sources;
    }

    public String getIdentifier()
    {
        return directory.getFileName().toString();
    }

    public boolean isCompiled()
    {
        for (JavaSource s : sources)
        {
            if(!s.isCompiled())
                return false;
        }
        return true;
    }

    public void addFlag(Flag flag)
    {
        flags.add(flag);
    }
}
