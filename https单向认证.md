## Tomcat https单向认证

### 1、制作证书

1. 生成服务端keystore

   使用java 工具keytool 为Tomcat生成证书 

   ```sh
   keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 -validity 36500 -keypass 123456 -keystore tomcat.keystore -storepass 123456
   : '
   您的名字与姓氏是什么?
     [Unknown]:  这是必填项，并且必须是TOMCAT部署主机的域名或者IP[如：gbcom.com 或者 10.1.25.251]
   您的组织单位名称是什么?
     [Unknown]:  localhost
   您的组织名称是什么?
     [Unknown]:  localhost
   您所在的城市或区域名称是什么?
     [Unknown]:  hz
   您所在的省/市/自治区名称是什么?
     [Unknown]:  zj
   该单位的双字母国家/地区代码是什么?
     [Unknown]:  cn
   CN=localhost, OU=localhost, O=localhost, L=hz, ST=zj, C=cn是否正确?
     [否]:  y 
   '
   ```

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/jfGJb2kaE4.png?imageslim)

2. 导出证书并导入浏览器

   ```sh
   keytool -export -alias tomcat -file tomcat.cer -keystore tomcat.keystore -storepass 123456
   ```

   双击tomcat.cer证书，导入到受信任的根证书颁发机构。

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/kGIlKkD2AC.png?imageslim)

3. 查看公钥内容

   ```sh
   keytool -printcert -rfc -file tomcat.cer
   // -rfc rfc样式
   ```

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180816/GIeEjB7eFJ.png?imageslim)

### 2、Tomcat 配置

1. 配置server.xml

   由于是单向认证，没有必要生成客户端的证书，直接进入Tomcat目录 conf 下配置 server.xml文件

   ```xml
   <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
              maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
              clientAuth="false" sslProtocol="TLS" 
              keystoreFile="${catalina.base}/key/tomcat.keystore" keystorePass="123456"/>
   ```

   <Connector>配置里的一些属性参数如下表：

   | **属　性**   | **描　　述**                                                 |
   | ------------ | ------------------------------------------------------------ |
   | clientAuth   | 如果设为true，表示Tomcat要求所有的SSL客户出示安全证书，对SSL客户进行身份验证。即true 为双向认证，false 为单向认证。 |
   | keystoreFile | 指定keystore文件的存放位置，可以指定绝对路径，也可以指定相对于<CATALINA_HOME> （Tomcat安装目录）环境变量的相对路径。如果此项没有设定，默认情况下，Tomcat将从当前操作系统用户的用户目录下读取名为 “.keystore”的文件。 |
   | keystorePass | 指定keystore的密码，如果此项没有设定，在默认情况下，Tomcat将使用“changeit”作为默认密码。 |
   | sslProtocol  | 指定套接字（Socket）使用的加密/解密协议，默认值为TLS，用户不应该修改这个默认值。 |
   | ciphers      | 指定套接字可用的用于加密的密码清单，多个密码间以逗号（,）分隔。如果此项没有设定，在默认情况下，套接字可以使用任意一个可用的密码。 |

2. 访问CN（即证书name）验证

   ![mark](http://oymp4z5xr.bkt.clouddn.com/hexo/180815/1jd7a458ff.png?imageslim)

 访问成功！

### 3、Android Retrofit+Okhttps3 配置https

将上面生成的证书tomcat.cer 导入到android项目**assets**目录下

Andorid 读取自定义证书创建证书自定义的SSLSocketFactory，trustManager

```java
 /**
  * 通过okhttpClient来设置证书
  * 实现https 单向认证
  *
  * @param clientBuilder OKhttpClient.builder
  */
 public static void setCertificates(OkHttpClient.Builder clientBuilder) {
     try {
         // 读取证书的
         InputStream inputStream = App.getAppContext().getAssets().open("tomcat.cer");
         // 使用x509 证书工厂
         CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
         // 获取x509格式的证书
         Certificate certificate = certificateFactory.generateCertificate(inputStream);
         // 使用默认证书库加载证书
         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         keyStore.load(null);
         keyStore.setCertificateEntry("tomcat", certificate);
         try {
             inputStream.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         // 使用信任管理器默认的算法，加载认证书库
         TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         trustManagerFactory.init(keyStore);
         TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
         if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
             throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
         }
         //初始化SSLContext
         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
         SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
         X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
         clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
```

为okhttpClient.Builder 配置sslSocketFactory

```java
 /**
  * 构建OkHttpClient.Builder
  *
  * @return
  */
 private OkHttpClient.Builder getHttpClientBuilder() {
     OkHttpClient.Builder builder = new OkHttpClient.Builder();
     // 设置https
     SSLHelper.setCertificates(builder);
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

