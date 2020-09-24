package com.flybycloud.autoBuildPackage.action;

import com.intellij.ide.FileSelectInContext;
import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.PackageChooser;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilKt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.io.fs.FilePath;
import com.intellij.util.io.zip.JBZipFile;
import com.intellij.webcore.packaging.PackageManagementService;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;

public class BiuBiuBuildPackageAction extends AnAction {

    public static final String JS_FILE_EXTENSION = "js";

    public static final String XML_FILE_EXTENSION = "xml";

    public static final String JAVA_FILE_EXTENSION = "java";

    public static final String WEB_INF = "WEB-INF";
    public static final String CLASSES = "classes";
    public static final String COM = "com";
    public static final String TARGET = "target";

    @Override
    public void actionPerformed(AnActionEvent e) {

        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);

        VirtualFile chooseFile = FileChooser.chooseFile(fileChooserDescriptor, e.getProject(), null);

        String rootPath = chooseFile.getPath();

        Object[] data = e.getData(PlatformDataKeys.SELECTED_ITEMS);

        int i = 0;
        for (Object o:data){
            System.out.println(o);
            if(o instanceof PsiJavaFile){

                i++;
                PsiJavaFile f = (PsiJavaFile)o;
                buildPakcageForPsiClassFile(f, rootPath);
            }else if(o instanceof PsiClass){

                i++;
                PsiClass javaFile = (PsiClass) o;

                buildPackageForPsiJavaFile(javaFile, rootPath );
            }else if(o instanceof XmlFile){
                i++;
                XmlFile file = (XmlFile)o;
                VirtualFile virtualFile = file.getVirtualFile();
                String parentFilePath = virtualFile.getParent().getPath();
                buildPakcageForPsiFile(virtualFile.getPath(), FileUtil.join(rootPath, WEB_INF, CLASSES, parentFilePath.substring(parentFilePath.indexOf(COM))));
            }else if(o instanceof PsiFile){
                PsiFile file = (PsiFile) o;
                String defaultExtension = file.getFileType().getDefaultExtension();
                VirtualFile virtualFile = file.getVirtualFile();
                String parentFilePath = virtualFile.getParent().getPath();
                if (JS_FILE_EXTENSION.equals(defaultExtension)){

                    i++;
                    buildPakcageForPsiFile(virtualFile.getPath(), FileUtil.join(rootPath, parentFilePath.substring(parentFilePath.indexOf(WEB_INF))));
                }
            }else if(o instanceof HtmlFileImpl){

            }
        }
        if (i > 0){
            Messages.showMessageDialog("打包成功，快去桌面看看有没有！", "Information", Messages.getInformationIcon());
        }else {
            Messages.showMessageDialog("请选择有效的class文件！", "Information", Messages.getInformationIcon());
        }
    }

    private void buildPackageForPsiJavaFile(PsiClass javaFile, String rootPath){
        Project project = javaFile.getProject();

        PsiJavaFile psiJavaFile = (PsiJavaFile) javaFile.getContainingFile();

        Module module = ModuleUtil.findModuleForFile(psiJavaFile.getVirtualFile(), project);

        String moduleRootPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();

        String packageName = psiJavaFile.getPackageName();
        String packageRelationPath = packageName.replaceAll("\\.", File.separator);

        String className = psiJavaFile.getName();

        String targetClassPath = FileUtil.join(moduleRootPath, TARGET, CLASSES, packageRelationPath, className.replaceAll(JAVA_FILE_EXTENSION, "class"));

        String fileDir = FileUtil.join(rootPath, WEB_INF, CLASSES, packageRelationPath);
        File file = new File(fileDir);
        if(!file.exists()){
            file.mkdirs();
        }

        try {
            File of = new File(targetClassPath);
            FileUtil.copyFileOrDir(of, new File(FileUtil.join(fileDir, of.getName())));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void buildPakcageForPsiClassFile(PsiJavaFile f, String rootPath){
        String packageName = f.getPackageName();
        VirtualFile virtualFile = f.getVirtualFile();

        String fileDir = FileUtil.join(rootPath, WEB_INF, CLASSES, packageName.replace(".", File.separator));
        File file = new File(fileDir);
        if(!file.exists()){
            file.mkdirs();
        }
        try {
            File of = new File(virtualFile.getPath());
            FileUtil.copyFileOrDir(of, new File(FileUtil.join(fileDir, of.getName())));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param filePath
     * @param fileDir
     */
    private void buildPakcageForPsiFile(String filePath, String fileDir){
         try{
            File xmlFile = new File(fileDir);
            if(!xmlFile.exists()){
                xmlFile.mkdirs();
            }
            File of = new File(filePath);
            FileUtil.copyFileOrDir(of, new File(FileUtil.join(fileDir, of.getName())));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
