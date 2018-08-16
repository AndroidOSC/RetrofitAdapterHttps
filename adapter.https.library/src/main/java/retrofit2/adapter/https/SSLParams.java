package retrofit2.adapter.https;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 包装的 SSL(Secure Socket Layer)
 *
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class SSLParams {
    public SSLSocketFactory sSLSocketFactory;
    public X509TrustManager trustManager;
}