-keepattributes SourceFile,LineNumberTable
-dontobfuscate
-dontoptimize
-keep public class de.mm20.launcher2.ui.launcher.search.common.SearchableItemVM {
    public <init>();
}

# ListItemViewModelStore creates its view models reflectively through the no-argument constructor.
-keep public class * extends de.mm20.launcher2.ui.launcher.search.ListItemViewModel {
    public <init>();
}
