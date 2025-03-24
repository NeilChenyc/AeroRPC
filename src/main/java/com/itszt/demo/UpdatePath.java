///**
// *
// */
//package com.itszt.demo.netty.demo;
//
//import java.io.IOException;
//
//import com.ice.jni.registry.NoSuchKeyException;
//import com.ice.jni.registry.RegStringValue;
//import com.ice.jni.registry.Registry;
//import com.ice.jni.registry.RegistryException;
//import com.ice.jni.registry.RegistryKey;
//
///**
// * @author Mr·wang
// *
// * TODO
// */
//public class UpdatePath
//{
//	public static void updatePath(String path)
//	{
//		String path_old_value = System.getenv("path");
//
//		try
//		{
//			RegistryKey openPath = Registry.HKEY_LOCAL_MACHINE.openSubKey("SYSTEM\\CurrentControlSet\\Control\\Session Manager");
//
//			RegistryKey subKey = openPath.createSubKey("Environment", "");
//
//			String path_new_value = path_old_value + path;
//
//			subKey.setValue(new RegStringValue(subKey,"path",path_new_value));
//
//			subKey.closeKey();
//
//			Runtime.getRuntime().exec("taskkill explorer.exe");
//
//			System.out.println("xxxx");
//
//			Thread.sleep(500);
//
//			Runtime.getRuntime().exec("explorer.exe");
//		} catch (NoSuchKeyException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (RegistryException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
