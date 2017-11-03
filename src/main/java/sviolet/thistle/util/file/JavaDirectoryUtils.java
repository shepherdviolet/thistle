package sviolet.thistle.util.file;

import java.io.InputStream;

/**
 * Java路径工具
 */
public class JavaDirectoryUtils {

    /**
     * 获得项目工程路径
     */
    public static String getProjectDir(){
        return System.getProperty("user.dir");
    }

    /**
     * 获得操作系统用户路径
     */
    public static String getUserDir(){
        return System.getProperty("user.home");
    }

    /**
     * 获得资源路径
     */
    public static String getResourceDir(){
        return Class.class.getResource("/").getPath();
    }

    /**
     * 获得资源的输入流
     * @param resourcePath 资源路径
     * @return 如果返回null表示资源不存在
     */
    public static InputStream getResourceInputStream(String resourcePath){
        return Class.class.getResourceAsStream(resourcePath);
    }

}
