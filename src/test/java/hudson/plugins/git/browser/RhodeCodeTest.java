package hudson.plugins.git.browser;

import hudson.plugins.git.GitChangeLogParser;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.SAXException;


public class RhodeCodeTest extends TestCase {

    /**
     *
     */
    private static final String RHODECODE_URL = "https://SERVER/r/PROJECT";
    private final RhodeCode rhodecode;

    {
        try {
            rhodecode = new RhodeCode(RHODECODE_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.RhodeCode#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrl() throws MalformedURLException {
        assertEquals(String.valueOf(rhodecode.getUrl()), RHODECODE_URL  + "/");
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.RhodeCode#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrlForRepoWithTrailingSlash() throws MalformedURLException {
        assertEquals(String.valueOf(new RhodeCode(RHODECODE_URL + "/").getUrl()), RHODECODE_URL  + "/");
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.RhodeCode#getChangeSetLink(hudson.plugins.git.GitChangeSet)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetChangeSetLinkGitChangeSet() throws IOException, SAXException {
        final URL changeSetLink = rhodecode.getChangeSetLink(createChangeSet("rawchangelog"));
        assertEquals(RHODECODE_URL + "/files/396fc230a3db05c427737aa5c2eb7856ba72b05d", changeSetLink.toString());
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.RhodeCode#getDiffLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetDiffLinkPath() throws IOException, SAXException {
        final HashMap<String, Path> pathMap = createPathMap("rawchangelog");
        final Path modified1 = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        assertEquals(RHODECODE_URL + "/files/396fc230a3db05c427737aa5c2eb7856ba72b05d/diffs?diffmode=sidebyside&fragment=1#src/main/java/hudson/plugins/git/browser/GithubWeb.java", rhodecode.getDiffLink(modified1).toString());
        // For added files returns a link to the commit.
        final Path added = pathMap.get("src/test/resources/hudson/plugins/git/browser/rawchangelog-with-deleted-file");
        assertEquals(RHODECODE_URL + "/files/396fc230a3db05c427737aa5c2eb7856ba72b05d/diffs?diffmode=sidebyside&fragment=1#src/test/resources/hudson/plugins/git/browser/rawchangelog-with-deleted-file", rhodecode.getDiffLink(added).toString());
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GithubWeb#getFileLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPath() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog");
        final Path path = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        final URL fileLink = rhodecode.getFileLink(path);
        assertEquals(RHODECODE_URL  + "/files/396fc230a3db05c427737aa5c2eb7856ba72b05d/src/main/java/hudson/plugins/git/browser/GithubWeb.java", String.valueOf(fileLink));
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GithubWeb#getFileLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPathForDeletedFile() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog-with-deleted-file");
        final Path path = pathMap.get("bar");
        final URL fileLink = rhodecode.getFileLink(path);
        assertEquals(RHODECODE_URL + "/files/fc029da233f161c65eb06d0f1ed4f36ae81d1f4f/diffs?diffmode=sidebyside&fragment=1#bar", String.valueOf(fileLink));
    }

    private GitChangeSet createChangeSet(String rawchangelogpath) throws IOException, SAXException {
        final File rawchangelog = new File(RhodeCodeTest.class.getResource(rawchangelogpath).getFile());
        final GitChangeLogParser logParser = new GitChangeLogParser(false);
        final List<GitChangeSet> changeSetList = logParser.parse(null, rawchangelog).getLogs();
        return changeSetList.get(0);
    }

    /**
     * @param changelog
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private HashMap<String, Path> createPathMap(final String changelog) throws IOException, SAXException {
        final HashMap<String, Path> pathMap = new HashMap<String, Path>();
        final Collection<Path> changeSet = createChangeSet(changelog).getPaths();
        for (final Path path : changeSet) {
            pathMap.put(path.getPath(), path);
        }
        return pathMap;
    }


}
