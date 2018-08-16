package retrofit2.adapter.https;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * @author atomOne
 */
public class HttpsHelpter {

    private final static String KEYSTORE_TYPE      = "BKS";
    private final static String PROTOCOL_TYPE      = "TLS";
    private final static String CERTIFICATE_FORMAT = "X509";

    /**
     * 为okhttpClient配置ssl,实现https单向认证
     *
     * @param clientBuilder OKhttpClient.builder
     * @param inputStream   证书内容
     * @param alias         证书别名
     */
    public static void buildOkhttpSSLOneAuth(OkHttpClient.Builder clientBuilder, InputStream inputStream, String alias) {
        try {
            // 使用x509 证书工厂, X.509 是Android唯一支持的证书格式
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // 获取x509格式的证书
            Certificate certificate = certificateFactory.generateCertificate(inputStream);
            inputStream.close();
            // 使用默认证书库加载证书,KeyStore 默认类型是 BKS，虽然 Android 的文档中的例子写了 JKS，但是 Android 并不支持JKS.
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry(alias, certificate);
            // 使用信任管理器默认的算法(默认算法 PKIX)，加载认证书库
            // TrustManager 是证书校验的关键，如果不使用系统默认校验方式时，需要开发者自己实现接口，完成校验代码
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            //初始化SSLContext, Android 不仅支持 TLS，还有 TLSv1.1 TLSv1.2 等，TLSv1.2需要 API levels 20+
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 为okhttpClient配置ssl,实现https双向认证
     *
     * @param clientBuilder OKhttpClient.builder
     * @param clientCer     客户端证书
     * @param clientPass    客户端证书密码
     * @param serverTrust   服务端信任证书
     * @param trustPass     服务端信任证密码
     */
    public static void buildOkhttpSSLTwoAuth(OkHttpClient.Builder clientBuilder, InputStream clientCer, String clientPass, InputStream serverTrust, String trustPass) {
        try {
            // 客户端的keystore,服务器端需要验证
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(clientCer, clientPass.toCharArray());
            trustStore.load(serverTrust, trustPass.toCharArray());
            clientCer.close();
            serverTrust.close();
            // 初始化SSLContext
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TYPE);
            // 使用x509算法，初始化信任工厂，加载信任认证书库
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(CERTIFICATE_FORMAT);
            trustManagerFactory.init(trustStore);
            // 使用x509算法,初始化证书工厂，加载客户端证书
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(CERTIFICATE_FORMAT);
            keyManagerFactory.init(keyStore, clientPass.toCharArray());
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
}
