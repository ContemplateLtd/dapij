package agent;

import java.io.File;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import org.junit.Assert;
import org.junit.Test;

/* TODO: Test (Settings) for concurrency. */

/**
 * Includes tests for the the {@link Settings} singleton class.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class SettingsTest {

    @Test
    public void settingsBasicTest() throws Exception {
        Settings s = Settings.INSTANCE;
        String settNm1 = "s1", settVl1 = "v1", settVl2 = "v2";
        s.set(settNm1, settVl1);

        /* Test whether setting inserted and can obtain same value2. */
        Assert.assertEquals("Setterly inserted: ", true, s.get(settNm1).equals(settVl1));

        /* Test whether setting successfully overwritten. */
        s.set(settNm1, settVl2);
        Assert.assertEquals("Setterly overwritten: ", true, s.get(settNm1).equals(settVl2));

        /* Test whether root project path is correct. */
        ProtectionDomain pd = this.getClass().getProtectionDomain();
        try {
            File pkgDir = new File(pd.getCodeSource().getLocation().toURI());
            Assert.assertEquals("Is root path valid: ", true,
                    s.get(Settings.SETT_CWD).equals(pkgDir.getParentFile().getParent()));
        } catch (URISyntaxException e) {
            System.out.println("Could not obtain project root path.");
            throw new RuntimeException(e);
        }

        /* Test whether unsetSett properly removes a setting. */
        s.set(settNm1, settVl1);
        s.rm(settNm1);
        Assert.assertEquals("Settings successfully removed: ", true, s.isSet(settNm1) == false);
    }
}
