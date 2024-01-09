package com.electrolytej.vi;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J \u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0016\u00a8\u0006\u000f"}, d2 = {"Lcom/electrolytej/vi/UnusedResourceOptimizer;", "Lcom/electrolytej/vi/BaseOptimizer;", "()V", "processArsc", "", "resourceFile", "Lpink/madis/apk/arsc/ResourceFile;", "processRes", "srcFile", "Ljava/util/zip/ZipFile;", "destDir", "Ljava/io/File;", "zipEntry", "Ljava/util/zip/ZipEntry;", "Companion", "vi-optimizer-duplicated-files"})
@com.google.auto.service.AutoService(value = {com.electrolytej.vi.BaseOptimizer.class})
public final class UnusedResourceOptimizer implements com.electrolytej.vi.BaseOptimizer {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNUSED_PROPERTY_IGNORES = "vi.optimizer.unused.files.ignores";
    @org.jetbrains.annotations.NotNull()
    public static final com.electrolytej.vi.UnusedResourceOptimizer.Companion Companion = null;
    
    public UnusedResourceOptimizer() {
        super();
    }
    
    @java.lang.Override()
    public boolean processArsc(@org.jetbrains.annotations.NotNull()
    pink.madis.apk.arsc.ResourceFile resourceFile) {
        return false;
    }
    
    @java.lang.Override()
    public boolean processRes(@org.jetbrains.annotations.NotNull()
    java.util.zip.ZipFile srcFile, @org.jetbrains.annotations.NotNull()
    java.io.File destDir, @org.jetbrains.annotations.NotNull()
    java.util.zip.ZipEntry zipEntry) {
        return false;
    }
    
    @java.lang.Override()
    public void end(@org.jetbrains.annotations.NotNull()
    java.io.File ap_) {
    }
    
    @java.lang.Override()
    public void start(@org.jetbrains.annotations.NotNull()
    com.android.build.gradle.api.BaseVariant variant, @org.jetbrains.annotations.NotNull()
    com.electrolytej.vi.SymbolList symbols, @org.jetbrains.annotations.NotNull()
    com.electrolytej.vi.L logger, @org.jetbrains.annotations.NotNull()
    java.io.File ap_) {
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/electrolytej/vi/UnusedResourceOptimizer$Companion;", "", "()V", "UNUSED_PROPERTY_IGNORES", "", "vi-optimizer-duplicated-files"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}