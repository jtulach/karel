package cz.xelfi.karel;

import net.java.html.boot.BrowserBuilder;

public final class Main {
    private static Karel karel;
    private Main() {
    }
    
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(Main.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }

    /** Called when page is ready */
    public static Karel onPageLoad(String... args) throws Exception {
        String src = Storage.getDefault().get("source", "\n\n");
        final Scratch s = new Scratch();
        s.getTown().clear();
    
        karel = new Karel("home", "msg", null, null, s, src, 300, false);
        KarelModel.compile(karel, false);
        karel.applyBindings();
        
        return karel;
    }
}
