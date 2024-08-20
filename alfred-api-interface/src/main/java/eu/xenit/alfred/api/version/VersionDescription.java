package eu.xenit.alfred.api.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes the alfredApi implementation version
 */
public class VersionDescription {

    private static final Pattern versionPattern = Pattern.compile("([0-9]+).([0-9]+).([0-9]+)(-.+)?");

    private String version;
    private String description;

    private int major;
    private int minor;
    private int patch;


    public VersionDescription() {
    }

    public VersionDescription(String version, String description) {
        this.version = version;
        this.description = description;
    }

    public static VersionDescription createFromVersionString(String version, String description) {
        VersionDescription ret = new VersionDescription(version, description);

        int firstDot = version.indexOf('.');
        int secondDot = version.indexOf('.');

        Matcher matcher = versionPattern.matcher(version);
        if (!matcher.matches()) {
            throw new RuntimeException("Version is not correct format");
        }

        //matcher.find();

        ret.setMajor(Integer.parseInt(matcher.group(1)));
        ret.setMinor(Integer.parseInt(matcher.group(2)));
        ret.setPatch(Integer.parseInt(matcher.group(3)));

        return ret;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }
}
