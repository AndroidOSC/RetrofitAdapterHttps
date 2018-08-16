package retrofit2.adapter.https;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RawRes;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * https 工具类
 *
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class HttpsUtils {


    /**
     * 主机名校验方法
     */
    public static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return hostname.equalsIgnoreCase(session.getPeerHost());
            }
        };
    }

    /**
     * @param context        上下文
     * @param certificatesId "XXX.cer" 文件 (文件位置res/raw/XXX.cer)
     * @param bksFileId      "XXX.bks"文件(文件位置res/raw/XXX.bks)
     * @param password       The certificate's password.
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(Context context, @RawRes int[] certificatesId, @RawRes int bksFileId, String password) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        SSLParams sslParams = new SSLParams();
        try {
            TrustManager[] trustManagers = prepareTrustManager(context, certificatesId);
            KeyManager[] keyManagers = prepareKeyManager(context, bksFileId, password);

            //创建TLS类型的SSLContext对象，that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");

            X509TrustManager x509TrustManager;
            if (trustManagers != null) {
                x509TrustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else {
                x509TrustManager = new UnSafeTrustManager();
            }
            //用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
            sslContext.init(keyManagers, new TrustManager[]{x509TrustManager}, null);

            //通过sslContext获取SSLSocketFactory对象
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                /*Android 4.X 对TLS1.1、TLS1.2的支持*/
                sslParams.sSLSocketFactory = new Tls12SocketFactory(sslContext.getSocketFactory());
                sslParams.trustManager = x509TrustManager;
                return sslParams;
            }

            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = x509TrustManager;
            return sslParams;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new AssertionError(e);
        }
    }


    /**
     * 构建TrustManager[]
     *
     * @param context
     * @param certificatesId
     * @return
     */
    private static TrustManager[] prepareTrustManager(Context context, int[] certificatesId) {
        if (certificatesId == null || certificatesId.length <= 0) {
            return null;
        }
        try {
            //创建X.509格式的CertificateFactory
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // 创建一个默认类型的KeyStore，存储我们信任的证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (int certificateId : certificatesId) {
                //从本地资源中获取证书的流
                InputStream cerInputStream = context.getResources().openRawResource(certificateId);
                String certificateAlias = Integer.toString(index++);

                //certificate是java.security.cert.Certificate，而不是其他Certificate
                //证书工厂根据证书文件的流生成证书Certificate
                Certificate certificate = certificateFactory.generateCertificate(cerInputStream);
                //将证书certificate作为信任的证书放入到keyStore中
                keyStore.setCertificateEntry(certificateAlias, certificate);
                try {
                    if (cerInputStream != null) {
                        cerInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TrustManagerFactory是用于生成TrustManager的,这里创建一个默认类型的TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //用我们之前的keyStore实例初始化TrustManagerFactory，这样trustManagerFactory就会信任keyStore中的证书
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取keyManager[]
     *
     * @param context
     * @param bksFileId
     * @param password
     * @return
     */
    private static KeyManager[] prepareKeyManager(Context context, @RawRes int bksFileId, String password) {

        try {
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(context.getResources().openRawResource(bksFileId), password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();

        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取X509TrustManager
     *
     * @param trustManagers
     * @return
     */
    public static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }
}