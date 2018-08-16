package retrofit2.adapter.https;

import android.annotation.SuppressLint;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * 客户端不对证书做任何检查;
 * 存在很大的安全漏洞
 *
 * @author atomOne
 */
public class UnSafeTrustManager implements X509TrustManager {

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}