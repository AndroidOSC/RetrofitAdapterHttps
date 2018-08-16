

## Tomcat 自签名证书双向认证

### 一、证书制作

1. 生成服务端server.keystore

   ```sh
   keytool -genkey -v -alias server -keyalg RSA -validity 36500 -keypass 123456 -storepass 123456 -keystore server.keystore
   // -genkey
   // -v
   // -alias 自定义证书名称
   // -keyalg 加密算法
   // -validity 证书有效期，36500表示100年，默认值是90天
   // -keypass 证书密码，这项较为重要，会在tomcat配置文件中使用，建议输入与keystore的密码一致，设置其它密码也可以
   // -storepass keystore密码，此处需要输入大于等于6个字符的字符串
   // -keystore 证书文件路径和名称
   > “您的名字与姓氏是什么？”这是必填项，并且必须是TOMCAT部署主机的域名或者IP[如：gbcom.com 或者 10.1.25.251]（就是你将来要在浏览器中输入的访问地址），否则浏览器会弹出警告窗口，提示用户证书与所在域不匹配。在本地做开发测试时，应填入“localhost”。
   > 你的组织单位名称是什么？”、“您的组织名称是什么？”、“您所在城市或区域名称是什么？”、“您所在的州或者省份名称是什么？”、“该单位的两字母国家代码是什么？”可以按照需要填写也可以不填写直接回车，在系统询问“正确吗？”时，对照输入信息，如果符合要求则使用键盘输入字母“y”，否则输入“n”重新填写上面的信息。
   ```

   如图所示：

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/9cB8f2l6Ka.png?imageslim)

2. 为客户端生成证书 

   双向认证时需要客户端安装该证书 ，为浏览器生成证书，以便让服务器来验证它。为了能将证书顺利导入至IE和Firefox，证书格式应该是PKCS12。

   ```sh
   keytool -genkey -v -alias client -keyalg RSA -validity 3650 -storetype PKCS12 -keypass 123456 -storepass 123456  -keystore client.p12
   // -genkey
   // -v
   // -alias 自定义证书名称
   // -keyalg 加密算法
   // -validity 证书有效期，3650表示10年，默认值是90天
   // -storetype 证书格式
   // -keypass 证书密码，此处需要输入大于等于6个字符的字符串
   // -storepass keystore密码，此处需要输入大于等于6个字符的字符串
   // -keystore 证书文件路径和名称
   > 客户端的CN(您的名字与姓氏,服务端是host)可以是任意值。双击client.p12文件，即可将证书导入至浏览器（客户端）。
   ```

   如图所示：

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/c96ff3le3A.png?imageslim)

3. 导出客户端证书

   由于是双向SSL认证，服务器必须要信任客户端证书，因此，必须把客户端证书添加为服务器的信任认证 ，因为不能直接将PKCS12格式的证书库导入服务端证书（server.keystore）,所以 将p12文件导出为一个cer文件

   ```sh
   keytool -export -alias client -keystore client.p12 -storetype PKCS12 -storepass 123456 -rfc -file client.cer 
   // -export 导出
   // -alias 自定义证书名称
   // -keystore 证书文件路径和名称
   // -storetype 证书格式
   // -storepass keystore密码
   // -rfc 
   // -file 导出文件路径名称
   ```

   如图所示：

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/JLhe4DagKf.png?imageslim)

   

4. 让服务器信任客户端证书 

   是将 **client.cer** 导入到服务器的证书库 **server.keystore**， 一个keystore可以导入多个证书，生成证书列表。 

   ```sh
   keytool -import -v -alias client -file client.cer -keystore server.keystore -storepass 123456
   // -import
   // -v
   // -alias
   // -file
   // -keystore
   // -storepass
   ```

   如图所示

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/hEAeBm914f.png?imageslim)

   

5. 让客户端信任服务器证书

   由于是双向SSL认证，客户端也要验证服务器证书，因此，必须把服务器证书添加到浏览的“受信任的根证书颁发机构”。由于不能直接将keystore格式的证书库导入，必须先把服务器证书导出为一个单独的CER文件。

   ```sh
   keytool -export -alias server -keystore server.keystore -storepass 123456 -file server.cer
   // -export 导出
   // -alias 自定义证书名称
   // -keystore 证书文件路径和名称
   // -storetype 证书格式
   // -storepass keystore密码
   // -file 导出文件路径名称
   > 双击server.cer文件，按照提示安装证书，将证书填入到“受信任的根证书颁发机构”。
   ```

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/l60mgd98Dc.png?imageslim)

6. 查看服务端证书库

   ```sh
   keytool -list -keystore server.keystore -storepass 123456
   ```

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/dKe59mK77d.png?imageslim)

### 二、Tomcat 配置

在Tomcat 目录,打开conf 文件夹 下的server.xml。配置方法如下：

``` xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
           clientAuth="true" sslProtocol="TLS"
           keystoreFile="${catalina.base}/key/server.keystore" keystorePass="123456"
           truststoreFile="${catalina.base}/key/server.keystore" truststorePass="123456"/>

属性说明：
clientAuth:设置是否双向验证，默认为false，设置为true代表双向验证
keystoreFile:服务器证书文件路径
keystorePass:服务器证书密码
truststoreFile:用来验证客户端证书的根证书，此例中就是服务器证书
truststorePass:根证书密码
```

重新启动Tomcat， 确保  将 server.cer 导入到浏览器“受信任的根证书颁发机构”区域，这样是为了让浏览器信任服务器。将  client.p12文件，即可将证书导入至浏览器“个人”区域，这样是为了让服务器信任浏览器。在浏览器输入https://192.168.1.112:8443/  即可正常访问。地址栏后会有“锁”图标，表示本次会话已经通过HTTPS双向验证，接下来的会话过程中所传输的信息都已经过SSL信息加密。 

![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/454AdK3K5H.png?imageslim)

### 三、Android 端使用 https 实现双向认证

1. 生成客户端信任证书

   有服务端证书导出客户端信任证书

   ```sh
   keytool -import -v -alias server -file server.cer -keystore truststore.jks -storepass 123456 
   // -import 导入
   // -v
   // -alias 
   // -file
   // -keystore 
   // -storepass keystore密码
   ```

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/B27f4bi9Ec.png?imageslim)

   

2. 生成Android识别的BKS库文件 

   Java平台默认识别jks格式的证书文件，但是android平台只识别bks格式的证书文件 。用[Portecle](https://sourceforge.net/projects/portecle/files/latest/download?source=files)工具转成bks格式。 

   运行protecle.jar将client.p12和truststore.jks分别转换成client.bks和truststore.bks,然后放到android客户端的assert目录下 。

   操作方式：File -> open Keystore File -> 选择证书库文件 -> 输入密码 -> Tools -> change keystore type -> BKS -> save keystore as -> 保存即可 

3. 使用Retrofit2 + Okhttp3 实现Https双向认证。

   Andorid 读取自定义证书创建证书自定义的SSLSocketFactory，trustManager

   ```java
    private final static String CLIENT_PRI_KEY          = "client.bks";
    private final static String TRUSTSTORE_PUB_KEY      = "truststore.bks";
    private final static String CLIENT_BKS_PASSWORD     = "123456";
    private final static String TRUSTSTORE_BKS_PASSWORD = "123456";
    private final static String KEYSTORE_TYPE           = "BKS";
    private final static String PROTOCOL_TYPE           = "TLS";
    private final static String CERTIFICATE_FORMAT      = "X509";
    /**
     * 为okhttpClient配置ssl
     * 实现双向认证
     *
     * @param clientBuilder
     */
    public static void buildOkhttpSSL(OkHttpClient.Builder clientBuilder) {
        try {
            // 客户端的keystore,服务器端需要验证
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            // 读取并加载证书
            InputStream ksIn = App.getAppContext().getAssets().open(CLIENT_PRI_KEY);
            InputStream tsIn = App.getAppContext().getAssets().open(TRUSTSTORE_PUB_KEY);
            keyStore.load(ksIn, CLIENT_BKS_PASSWORD.toCharArray());
            trustStore.load(tsIn, TRUSTSTORE_BKS_PASSWORD.toCharArray());
            ksIn.close();
            tsIn.close();
            // 初始化SSLContext
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TYPE);
            // 使用x509算法，初始化信任工厂，加载信任认证书库
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(CERTIFICATE_FORMAT);
            trustManagerFactory.init(trustStore);
            // 使用x509算法,初始化证书工厂，加载客户端证书
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(CERTIFICATE_FORMAT);
            keyManagerFactory.init(keyStore, CLIENT_BKS_PASSWORD.toCharArray());
            //获得SSLSocketFactory
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            //获得trustManager
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   ```

   设置okhttp

   ```java
       /**
        * 构建OkHttpClient.Builder
        *
        * @return
        */
       private OkHttpClient.Builder getHttpClientBuilder() {
           OkHttpClient.Builder builder = new OkHttpClient.Builder();
           // 设置https
           SSLHelper.buildOkhttpSSL(builder, App.getAppContext());
           // 设置hostnameVerifier
           builder.hostnameVerifier(new HostnameVerifier() {
               @Override
               public boolean verify(String hostname, SSLSession session) {
                   Log.i("OkHttpHelper", "hostname =" + hostname);
                   Log.i("OkHttpHelper", "session =" + session.getPeerHost());
                   return hostname.equalsIgnoreCase(session.getPeerHost());
               }
           });
           // 添加log
           if (AppConfig.DEBUG) {
               HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
               interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
               if (!builder.interceptors().contains(interceptor)) {
                   builder.addInterceptor(interceptor);
               }
           }
           return builder;
       }
   ```

4. 通过以上配置，android 就可用访问自签名证书实现的https双向认证的api了。