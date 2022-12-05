package com.cyc.shell;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ShellApplication extends Application {

    private static final String TAG = ShellApplication.class.getCanonicalName();

    static {
        System.loadLibrary("secretkey-lib");
    }

    public native String getSecretKey();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        AES.init(getSecretKey());
        File apkFile = new File(getApplicationInfo().sourceDir);
        //data/data/包名/files/fake_apk/
        File unZipFile = getDir("fake_apk", MODE_PRIVATE);
        File app = new File(unZipFile, "app");
        boolean flag = Zip.unZipAndDecrypt(apkFile, app);
        if(!flag){
            return;
        }
        List list = new ArrayList<>();
        Log.d(TAG, Arrays.toString(app.listFiles()));
        for (File file : app.listFiles()) {
            if (file.getName().endsWith(".dex")) {
                list.add(file);
            }
        }

        Log.d(TAG, list.toString());
        try {
            V19.install(getClassLoader(), list, unZipFile);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "attachBaseContext END");
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field e = clazz.getDeclaredField(name);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    private static Method findMethod(Object instance, String name, Class... parameterTypes)
            throws NoSuchMethodException {
        Class clazz = instance.getClass();
//        Method[] declaredMethods = clazz.getDeclaredMethods();
//        System.out.println("  findMethod ");
//        for (Method m : declaredMethods) {
//            System.out.print(m.getName() + "  : ");
//            Class<?>[] parameterTypes1 = m.getParameterTypes();
//            for (Class clazz1 : parameterTypes1) {
//                System.out.print(clazz1.getName() + " ");
//            }
//            System.out.println("");
//        }
        while (clazz != null) {
            try {
                Method e = clazz.getDeclaredMethod(name, parameterTypes);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchMethodException var5) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList
                (parameterTypes) + " not found in " + instance.getClass());
    }

    private static void expandFieldArray(Object instance, String fieldName, Object[]
            extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(original.getClass()
                .getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static final class V19 {
        private V19() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory) throws IllegalArgumentException,
                IllegalAccessException, NoSuchFieldException, InvocationTargetException,
                NoSuchMethodException {

            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            Log.d(TAG, "Build.VERSION.SDK_INT " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= 23) {
                expandFieldArray(dexPathList, "dexElements", makePathElements(dexPathList, new
                                ArrayList(additionalClassPathEntries), optimizedDirectory,
                        suppressedExceptions));
            } else {
                expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new
                                ArrayList(additionalClassPathEntries), optimizedDirectory,
                        suppressedExceptions));
            }

            if (suppressedExceptions.size() > 0) {
                Iterator suppressedExceptionsField = suppressedExceptions.iterator();

                while (suppressedExceptionsField.hasNext()) {
                    IOException dexElementsSuppressedExceptions = (IOException)
                            suppressedExceptionsField.next();
                    Log.w("MultiDex", "Exception in makeDexElement",
                            dexElementsSuppressedExceptions);
                }

                Field suppressedExceptionsField1 = findField(loader,
                        "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions1 = (IOException[]) suppressedExceptionsField1.get(loader);
                if (dexElementsSuppressedExceptions1 == null) {
                    dexElementsSuppressedExceptions1 = (IOException[]) suppressedExceptions
                            .toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[suppressedExceptions.size() +
                            dexElementsSuppressedExceptions1.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions1, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions1.length);
                    dexElementsSuppressedExceptions1 = combined;
                }

                suppressedExceptionsField1.set(loader, dexElementsSuppressedExceptions1);
            }

        }

        private static Object[] makeDexElements(Object dexPathList,
                                                ArrayList<File> files, File
                                                        optimizedDirectory,
                                                ArrayList<IOException> suppressedExceptions) throws
                IllegalAccessException, InvocationTargetException, NoSuchMethodException {

            Method makeDexElements = findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class, ArrayList.class);
            return ((Object[]) makeDexElements.invoke(dexPathList, new Object[]{files,
                    optimizedDirectory, suppressedExceptions}));
        }
    }

    /**
     * A wrapper around
     * {@code private static final dalvik.system.DexPathList#makePathElements}.
     */
    private static Object[] makePathElements(
            Object dexPathList, ArrayList<File> files, File optimizedDirectory,
            ArrayList<IOException> suppressedExceptions)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method makePathElements;
        try {
            makePathElements = findMethod(dexPathList, "makePathElements", List.class, File.class,
                    List.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException: makePathElements(List,File,List) failure");
            try {
                makePathElements = findMethod(dexPathList, "makePathElements", ArrayList.class, File.class, ArrayList.class);
            } catch (NoSuchMethodException e1) {
                Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                try {
                    Log.e(TAG, "NoSuchMethodException: try use v19 instead");
                    return V19.makeDexElements(dexPathList, files, optimizedDirectory, suppressedExceptions);
                } catch (NoSuchMethodException e2) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                    throw e2;
                }
            }
        }
        return (Object[]) makePathElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        Log.i(TAG, "--------onCreate");
        //获取配置在清单文件的源Apk的Application路劲
        String appClassName = null;
        try {
            ApplicationInfo ai = this.getPackageManager()
                    .getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey("APPLICATION_CLASS_NAME")) {
                appClassName = bundle.getString("APPLICATION_CLASS_NAME");//className 是配置在xml文件中的。
            } else {
                Log.i(TAG, "have no application class name");
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "error:"+ Log.getStackTraceString(e));
            e.printStackTrace();
        }

        //获取当前壳Apk的ApplicationInfo
        Object currentActivityThread =
                RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[] {}, new Object[] {});
        Object mBoundApplication =
                RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        Object loadedApkInfo =
                RefInvoke.getFieldOjbect(
                "android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");
        //将LoadedApk中的ApplicationInfo设置为null

        RefInvoke.setFieldOjbect("android.app.LoadedApk", "mApplication",
                loadedApkInfo, null);

        //获取currentActivityThread中注册的Application
        Object oldApplication =
                RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mInitialApplication");

        //获取ActivityThread中所有已注册的Application，并将当前壳Apk的Application从中移除
        ArrayList<Application> mAllApplications = (ArrayList<Application>)
                RefInvoke
                .getFieldOjbect("android.app.ActivityThread",
                        currentActivityThread, "mAllApplications");
        mAllApplications.remove(oldApplication);

        ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo)
                RefInvoke
                .getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
                        "mApplicationInfo");

        ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo)
                RefInvoke
                .getFieldOjbect("android.app.ActivityThread$AppBindData",
                        mBoundApplication, "appInfo");
        //替换原来的Application
        appinfo_In_LoadedApk.className = appClassName;
        appinfo_In_AppBindData.className = appClassName;

        //注册Application
        Application app = (Application)
                RefInvoke.invokeMethod(
                "android.app.LoadedApk", "makeApplication", loadedApkInfo,
                new Class[] { boolean.class, Instrumentation.class },
                new Object[] { false, null });

        //替换ActivityThread中的Application

        RefInvoke.setFieldOjbect("android.app.ActivityThread",
                "mInitialApplication", currentActivityThread, app);

        ArrayMap mProviderMap = (ArrayMap)
                RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mProviderMap");

        Iterator it = mProviderMap.values().iterator();
        while (it.hasNext()) {
            Object providerClientRecord = it.next();
            Object localProvider =
                    RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread$ProviderClientRecord",
                    providerClientRecord, "mLocalProvider");
            if(localProvider != null){
                RefInvoke.setFieldOjbect("android.content.ContentProvider",
                        "mContext", localProvider, app);
            }
        }
        Log.i(TAG, "Srcapp:"+app);
        app.onCreate();
    }
}
