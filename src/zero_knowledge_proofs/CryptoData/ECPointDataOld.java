package zero_knowledge_proofs.CryptoData;

import java.math.BigInteger;
import java.security.spec.EllipticCurve;
import java.util.Base64;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public final class ECPointDataOld extends CryptoData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5968736215439976858L;
	private BigInteger x;
	private BigInteger y;
	
	public ECPointDataOld(ECPoint p)
	{
		if(!p.isNormalized())
			p = p.normalize();
		
		if(p.getXCoord() == null)
		{
			x = y = null;
		}
		else
		{
			x = p.getAffineXCoord().toBigInteger();
			y = p.getAffineYCoord().toBigInteger();
		}
	}
	@Override
	public CryptoData[] getCryptoDataArray() {
		return null; 
	}
	
	@Override
	public ECPoint getECPointData(ECCurve c) {
		if(x == null) return c.getInfinity();
		ECPoint p = c.createPoint(x, y);
		 return p;
	}
	
	@Override
	public int size() {
		return 1;
	}

	@Override
	public String toString()
	{
		if(x == null) return "INF";
		return String.format("(%s,%s)", x.toString(16), y.toString(16));
	}
	
	@Override
	public String toString64()
	{
		if(x == null) return "INF";
		return String.format("(%s,%s)", Base64.getEncoder().encodeToString(x.toByteArray()),Base64.getEncoder().encodeToString(y.toByteArray()));
	}
}
