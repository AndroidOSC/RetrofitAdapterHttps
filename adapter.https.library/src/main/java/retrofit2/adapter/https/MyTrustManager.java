package retrofit2.adapter.https;

import android.annotation.SuppressLint;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 自定义实现证书信任管理
 *
 * @author atomOne
 */
public class MyTrustManager implements X509TrustManager {
    private X509TrustManager defaultTrustManager;
    private X509TrustManager localTrustManager;

    public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
        //TrustManagerFactory是用于生成TrustManager的,创建一个默认类型的TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        defaultTrustManager = HttpsUtils.chooseTrustManager(trustManagerFactory.getTrustManagers());
        this.localTrustManager = localTrustManager;
    }


    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {
            localTrustManager.checkServerTrusted(chain, authType);
        }
    }


    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }


}