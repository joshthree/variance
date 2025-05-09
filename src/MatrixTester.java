import java.math.BigInteger;

import zero_knowledge_proofs.CryptoData.Matrix;
import zero_knowledge_proofs.CryptoData.MatrixInterface;

public class MatrixTester {
	public static void main(String[] args) {
		BigInteger[][] m = {{BigInteger.valueOf(1),BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0)}, 
				{BigInteger.valueOf(1),BigInteger.valueOf(1), BigInteger.valueOf(1), BigInteger.valueOf(1)},
				{BigInteger.valueOf(1),BigInteger.valueOf(2), BigInteger.valueOf(4), BigInteger.valueOf(8)},
				{BigInteger.valueOf(1),BigInteger.valueOf(3), BigInteger.valueOf(9), BigInteger.valueOf(27)}
				};
		BigInteger prime = new BigInteger("115792089237316195423570985008687907852837564279074904382605163141518161494337");
		
		Matrix matrix = new Matrix(m, prime);
		
		Matrix inverse = (Matrix) matrix.getInverse();
		
		BigInteger[][] carryValues = new BigInteger[][]{{BigInteger.ZERO}, {BigInteger.ZERO},{BigInteger.ONE},{BigInteger.ONE}};
		BigInteger[][] resultValues = new BigInteger[][]{{BigInteger.ZERO}, {BigInteger.ONE},{BigInteger.ZERO},{BigInteger.ONE}};
		
		MatrixInterface goalCarryValues = new Matrix(carryValues, prime);
		MatrixInterface goalResultValues = new Matrix(resultValues, prime);
		
		MatrixInterface carryCoefficients = goalCarryValues.multiply(inverse);
		MatrixInterface resultCoefficients = goalResultValues.multiply(inverse);

		System.out.println("Result Coefficients:  ");
		System.out.println(resultCoefficients);
		
		BigInteger[][] c = resultCoefficients.getMatrix(); 
		for(int i = 0; i < 4; i++) {
			BigInteger x = BigInteger.valueOf(i);
			BigInteger result = BigInteger.ZERO;
			for(int j = 0; j < c.length; j++) {
				result = result.add(c[j][0].multiply(x.modPow(BigInteger.valueOf(j), prime))).mod(prime);
				System.out.print(x.modPow(BigInteger.valueOf(j), prime) + ", ");
				
			}
			
			System.out.println(result);
		}
		System.out.println("Carry Coefficients:  ");
		System.out.println(carryCoefficients);
		c = carryCoefficients.getMatrix();
		
		for(int i = 0; i < 4; i++) {
			BigInteger x = BigInteger.valueOf(i);
			BigInteger result = BigInteger.ZERO;
			for(int j = 0; j < c.length; j++) {
				result = result.add(c[j][0].multiply(x.modPow(BigInteger.valueOf(j), prime))).mod(prime);
				System.out.print(x.modPow(BigInteger.valueOf(j), prime) + ", ");
				
			}
			
			System.out.println(result);
		}
		
		
	}
}
