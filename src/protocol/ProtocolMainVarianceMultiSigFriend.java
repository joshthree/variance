package protocol;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import zero_knowledge_proofs.ArraySizesDoNotMatchException;
import zero_knowledge_proofs.ECSchnorrProver;
import zero_knowledge_proofs.MultipleTrueProofException;
import zero_knowledge_proofs.NoTrueProofException;
import zero_knowledge_proofs.VarianceToolkit;
import zero_knowledge_proofs.ZKPProtocol;
import zero_knowledge_proofs.CryptoData.BigIntData;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;
import zero_knowledge_proofs.CryptoData.ECCurveData;
import zero_knowledge_proofs.CryptoData.ECPointData;

public class ProtocolMainVarianceMultiSigFriend {

	public static void main(String[] args) {
		// TODO:  Make usage statement:  <executable> <ip> <port> <position> <accounts_filename> <key_filename> <environment_filename> [seed] 
		

		System.setProperty("javax.net.ssl.trustStore", "resources/Client_Truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "test123");
		System.setProperty("javax.net.ssl.keyStorePassword", "test123");
		System.setProperty("java.security.policy", "resources/mysecurity.policy");
		SocketFactory sf = SSLSocketFactory.getDefault();
		
		InputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		InputStream keyFile;
		InputStreamReader isr2;
		BufferedReader keyBr;
		InputStream envFile;
		InputStreamReader isr3;
		BufferedReader envBr;

		boolean flag = false;
		SecureRandom rand;
		if(args.length == 7)
			rand = new SecureRandom(new BigInteger(args[6]).toByteArray());
		else if(args.length == 6)
			rand = new SecureRandom();
		else {
			System.out.println("usage:  <executable> <ip> <port> <position on main party's list> <account_filename> <key_filename> <environment_filename> [seed]");
			System.exit(1);
			return;
		}
		try {
			fis = new FileInputStream("inputs/"+args[3]);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

			keyFile = new FileInputStream("inputs/"+args[4]);
			isr2 = new InputStreamReader(keyFile);
			keyBr = new BufferedReader(isr2);

			envFile = new FileInputStream("inputs/"+args[5]);
			isr3 = new InputStreamReader(envFile);
			envBr = new BufferedReader(isr3);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		String dataRow;

		
		String leaderIP = args[0];
		int leaderPort = Integer.parseInt(args[1]);
		Socket toLeader = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			dataRow = envBr.readLine();
			String[] envString = dataRow.split("\t");
			ECPoint g = ECNamedCurveTable.getParameterSpec(envString[0]).getG();
			ECCurve curve = g.getCurve();
			BigInteger order = g.getCurve().getOrder();
			Decoder decoder = Base64.getDecoder();
			System.out.println("Attempting to connect to main party");
			boolean flag2 = true;
			int blahman = 0;
			while(flag2)
			{
				try {
					toLeader = sf.createSocket(leaderIP, leaderPort);

					out = new ObjectOutputStream(toLeader.getOutputStream());
					in = new ObjectInputStream(toLeader.getInputStream());
					out.writeInt(Integer.parseInt(args[2]));
					out.flush();
					System.out.println("Sent number!");
					if(in.readBoolean()) {
						flag2=false;
						System.out.println("Success!");						
					}
					else {
						System.out.println("They said no.  Sleeping for 5 seconds");
						Thread.sleep(5000);
						System.out.println("Trying again 1...");
						toLeader.close();
						toLeader = null;
					}
				} catch (Exception e) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
//					e.printStackTrace(System.out);
//					System.out.println(leaderIP);
//					System.out.println(leaderPort);
//					System.out.println("Trying again 2...");
					if(blahman++ == 5) {
						System.out.println("friend having trouble");
					}
				}
			}
			CryptoData environment = new CryptoDataArray(new CryptoData[] {new ECCurveData(curve, g)});
			
			ArrayList<KeyList> knownKeys = new ArrayList<KeyList>();

			while((dataRow = keyBr.readLine()) != null)
			{
				//String format:  "Public Key	Private Key"
				 
				String[] stringData = dataRow.split("\t");
				if(stringData[0].length() > 2 && stringData[0].substring(0, 2).equals("//")) {
					continue;	//lines can be commented with //
				}
				if(stringData.length < 3) 
				{
					System.err.printf("Line rejected from Keys:  %s\n", dataRow);
					continue;				
				}
				
				
				KeyList keys = new KeyList();
				
				try
				{
					keys.id = Integer.parseInt(stringData[0]);
					keys.keys = new CryptoData[stringData.length / 2];
					keys.positions = new int[stringData.length / 2];
					for(int i = 0; i < keys.keys.length; i++)
					{
						keys.positions[i] = Integer.parseInt(stringData[((i+1) * 2) - 1]);
						keys.keys[i] = new BigIntData(new BigInteger(decoder.decode(stringData[(i+1) * 2])));
					}
					knownKeys.add(keys);
				}
				catch(Exception e)
				{
					System.err.println("Line failed in KeyFile:  Not parsed as numbers.  " + dataRow);
					e.printStackTrace(System.err);
					continue;
				}
			}		
			
			knownKeys.add(null);
			
			ZKPProtocol schnorr = new ECSchnorrProver();
			flag = in.readBoolean();
			int current = -1;
			String[] accountLine = null;
			int keyNum = 0;
			KeyList currKey = knownKeys.get(0);
			
			CryptoData[] input = new CryptoData[16];
			while(!flag)
			{ 
				int account = currKey.id;
				if(account < current){
					out.writeObject(null);
					out.flush();
					throw new IOException("Inputs out of order");
				}
				if(currKey == null || currKey.id != account)
				{
					out.writeObject(null);
					out.flush();
					throw new IOException("Unexpected Account");
				}
				while(current != account)
				{
					String line = br.readLine();
					if(line.length() == 0) continue;
					if(current + 1 == account)
					{
						accountLine = line.split("\t");
						if(accountLine[0].length() >= 2 && accountLine[0].substring(0, 2).equals("//")) {
							continue;	//lines can be commented with //
						}
						if(accountLine.length < 4) 
						{
							continue;				
						}
						
					}
					current++;
				}
				keyNum++;
				
				for(int i = 0; i < currKey.positions.length; i++)
				{
					input[i] = VarianceToolkit.createSchnorrProverInputsNoChecks(new ECPointData(curve.decodePoint(decoder.decode(accountLine[currKey.positions[i] + 3]))), currKey.keys[i], order, rand);
					CryptoData a;
					try {
						a = schnorr.initialComm(input[i], environment);
					} catch (MultipleTrueProofException | NoTrueProofException | ArraySizesDoNotMatchException e) {
						e.printStackTrace();
						out.writeObject(null);
						out.flush();
						return;
					}
					out.flush();
					out.writeObject(a);
				}
				BigInteger challenge;
				try {
					challenge = (BigInteger) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return;
				}
				
				for(int i = 0; i < currKey.positions.length; i++)
				{
					try {
						out.writeObject(schnorr.calcResponse(input[i], challenge, environment));
						out.flush();
					} catch (NoTrueProofException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MultipleTrueProofException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currKey = knownKeys.get(keyNum);
				flag = in.readBoolean();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	


		try {
			envBr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			keyBr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(flag)
			try {
				out.writeBoolean(true);
				toLeader.close();
			} catch (Exception e) {
			}
		System.out.println("Done! " + flag);
		System.exit(0);
	}

}
