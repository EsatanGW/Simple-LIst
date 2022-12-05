package com.cyc.shell;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Zip {

    public static void unZip(File zip, File dir) {
        try {
            dir.delete();
            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name
                        .equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean unZipAndDecrypt(File zip, File dir) {
        boolean flag = true;
        try {
            ZipFile zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if(zipFile.getEntry("apkname.txt") == null){
                Log.d("Zip","is normal install apk , without having to decrypt dex file");
                flag = false;
                return flag;
            }
            if(dir.exists()){
                String oldApkName = readTxtFile(new FileInputStream(dir.getAbsolutePath()+"/apkname.txt"));
                String newApkName = readTxtFile(zipFile.getInputStream(zipFile.getEntry("apkname.txt")));
                Log.i("Zip","unZipAndDecrypt oldApkName = "+oldApkName+" newApkName = "+newApkName);
                if(!TextUtils.isEmpty(oldApkName)&&!TextUtils.isEmpty(newApkName)){
                    if(oldApkName.equals(newApkName)){
                        Log.d("Zip","unZipAndDecrypt is same apk name , return");
                        return flag;
                    }
                }
            }

            dir.delete();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name
                        .equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zipFile.close();

            File[] files = dir.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (name.equals("classes.dex")) {

                } else if (name.endsWith(".dex")) {
                    try {
                        byte[] bytes = getBytes(file);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] decrypt = AES.decrypt(bytes);
//                        fos.write(bytes);
                        fos.write(decrypt);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static String readTxtFile(InputStream inputStream)
    {
        String content = ""; //文件内容字符串
        try {
            if (inputStream != null) {
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while (( line = buffreader.readLine()) != null) {
                    content += line + "\n";
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("Zip","readTxtFile error");
        }
        return content;
    }

    private static byte[] getBytes(File file) throws Exception {
        RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;
    }

}
