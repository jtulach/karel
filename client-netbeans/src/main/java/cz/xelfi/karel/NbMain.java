package cz.xelfi.karel;

import org.netbeans.api.nbrwsr.OpenHTMLRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

public class NbMain {
    private NbMain() {
    }
    
    @ActionID(
        category = "Games",
        id = "cz.xelfi.karel.OpenPage"
    )
    @OpenHTMLRegistration(
        url="index.html",
        displayName = "Open Your Page",
        iconBase = "cz/xelfi/karel/icon.png"
    )
    @ActionReferences({
        @ActionReference(path = "Menu/Window"),
        @ActionReference(path = "Toolbars/Games")
    })
    public static void onPageLoad() throws Exception {
        Main.onPageLoad();
    }
}
