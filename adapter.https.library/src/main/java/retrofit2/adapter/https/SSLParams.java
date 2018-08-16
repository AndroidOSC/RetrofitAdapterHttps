package retrofit2.adapter.https;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 包装的 SSL(Secure Socket Layer)
 *
 * @author atomOne
 */
public class SSLParams {
    public SSLSocketFactory sSLSocketFactory;
    public X509TrustManager trustManager;
}