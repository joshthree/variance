package zero_knowledge_proofs;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Base64;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import zero_knowledge_proofs.CryptoData.CryptoData;

public class ECPedersenCommitment implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4464353885259184169L;
	protected byte[] data;
	
	
	public ECPedersenCommitment(BigInteger message, BigInteger keys, CryptoData environment)
	{
		//g^m h^r 
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		ECPoint g = e[0].getECPointData(c);
		ECPoint h = e[1].getECPointData(c);
		ECPoint comm = g.multiply(message).add(h.multiply(keys)); 
		data = comm.getEncoded(false);
	}
	private ECPedersenCommitment(ECPoint comm)
	{
		data = comm.getEncoded(false);
	}

	public ECPoint getCommitment(CryptoData environment) {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		return c.decodePoint(data);
	}

	public boolean verifyCommitment(BigInteger message, BigInteger keys, CryptoData environment) {

		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		ECPoint g = e[0].getECPointData(c);
		ECPoint h = e[1].getECPointData(c);
		ECPoint comm = g.multiply(message).add(h.multiply(keys));
		return getCommitment(environment).equals(comm);
	}

	public ECPedersenCommitment multiplyCommitment(ECPedersenCommitment cmt, CryptoData environment) {
		
		return new ECPedersenCommitment(cmt.getCommitment(environment).add(getCommitment(environment)));
	}
	public ECPedersenCommitment multiplyShiftedCommitment(ECPedersenCommitment cmt, int lShift, CryptoData environment) {
		
		return new ECPedersenCommitment((cmt.getCommitment(environment).multiply(BigInteger.ONE.shiftLeft(lShift))).add(getCommitment(environment)));
	}
	
	public String toString64()
	{
		return String.format("(%s)", Base64.getEncoder().encodeToString(data));
	}

	public String toString()
	{
		return String.format("(%s)", new BigInteger(data));
	}
	public static ECPoint product(ECPedersenCommitment[] commitments, CryptoData environment) {
		if(commitments == null || commitments.length == 0)
		{
			return environment.getECCurveData().getInfinity();
		}
		ECPoint p = commitments[0].getCommitment(environment);
		for(int i = 1; i < commitments.length; i++) {
			p = p.add(commitments[i].getCommitment(environment));
		}
		return p;
	}
	
}
