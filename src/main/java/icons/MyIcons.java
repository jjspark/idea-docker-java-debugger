package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface MyIcons {
    Icon Debug = IconLoader.getIcon("/META-INF/docker-debug.svg", MyIcons.class);
    Icon Config = IconLoader.getIcon("/META-INF/settings.svg", MyIcons.class);
}
