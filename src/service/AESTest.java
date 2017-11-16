package service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import cn.gov.zjport.manchester.encrypt.AESEncrypt;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class AESTest {
	public static void main(String[] args){
		AESEncrypt aes = new AESEncrypt();
		String str = "b7a958ecf92c1ce94d58218ec396068cc771e1084e2e7450ba6fc5afa60b6ecd69244398508cd153fbd02afd62d83cdf57f1f6354a8e60a184aa538ba893b763bfe94c7e69d8f890e921514922ba6cbadd1687d5a199450f5f19f70f2d5069e95097ae70a25c4ed214a9ae7653f723380c41d7092d66f0346a22bc520e984d5dfe24a96410cb181ccd391ec68357d4dc4e1a812aedb0bc4ea0f8ff2c333603554d80e4f520f2377f584dd188b121acd109045f244f124a1c9dba154c4bab1b45f86ddc977475b0d1a89bb8b45ce97ef35cd5e76648adf873b65d0e712e1b3707fb94596dae7b767d5828921876c207840710d62161c581585333b60e45ee9145883b1bb755c92e5043e181d8f7e4b05f2112ac9a0fdc2b5302522348d6699e9a794d1650db83170a7fe1711e8fa9ffaad140881a3aae2a38239ef8dcc9ea53ccd9e803e868fa559033993592534c9f216cf28c205265b040628da8103f9138470a7e00c13e3d8894e9fa94b6bfd737513069423041ae84c8b86a3250f80e2f289c539413dc7f95f69c9ea2ca2a26da33a27bc0e32db5d0bcd4b69b6b0046abe5a5d514fa4f8d31320a7438ad3a65e3ba50267f2f4d9d6bb47eda2d7303ff3dbca7922c0c23c62726e69eb7ce36bd49f2943bfdc6dbcfacc54310b4c67c61f8b566c0ec64d5b34365c26a3cf4d394cd52d32a8f26dde146f7e3b0e4c83032099771475462704e300a2a00db99878b346c29e89ca9562fbf796474fea7d8df91ff2cde3ebc62e2b9fb6b9aec35b1ae1119f82f2326ef062fc973759aded3a38ead519fa4e66efd9928d7e16c6881ed1a64024393bad829cd08fbf0dc63a17324cbf79af6463b1765aabb8dcf68df8b6720643c39744ab3e58ed405b8466dcaad561a26c84aae09ff3af75895d46d1db14a1290d8419e63452027398c8d6069413fb674092670569a7b27de184191bb76d0f4a13944e3a680c990971e3c36f0c22845873799fc9c29221bed12c732c5791a16f89fa99636511e889e15084f0582900b12e19180304ba7e1aa221e85d639613f8adbddb880aa864a190a2d9e08b85d69c53ddd0d2f89e35727d36ea66f9682a27110deb5842b7610d6628ca79c8b9ed81a9f0df1a3d7f13f9bdc841b2bb0001ed3d5845fe1cb3496002b9ca19c64bdab84b7d7ed0e408122c5a40d1b2c365a22ba2eed46238a4ac023f0848a37cd24b962d5ec60132db6e4fabf12224102471e2f85ef447ddac6f6d702b22c80d83dc5bb8458de31d0453a3a636c75cad55d8fa30bb98ff080c23cdf65b2908b5a698a4a75a075b01301c894920a07a2640611536ee8132cfb5e659b6bb415fd368b5911cb1504d2f9abc21934a54b79e8bfef5723e9e3f5a81fdbfe514801d43759b48b1304b981e5e13a5bb338f993356165732c3a84882bb1eabadda1e057816b753159b15d3e80267991f5bde6479a9f0948915864d9a338e5a06e1ae05381baa7af4b92da66e071b5f4bffe835f4fa474702a9dbd24dd4ac41d489e51e84b9214c27408b9e6e9e541d9e474d54b07729b06888adced45116a33691f68a13aa3a37a5e37c336439a6a7711f11a8b6f565a66f70bb91828ad7eedd8efc64ec751f849dc8b9683e5e2381de33a0f702eb2c9c0d39e4da74d5bda530bb29700507e7f6273d65da6bcc83640cc80fc19b9a7091f2aa1430086d5df42e5f7780e055b4bd0b993799f17e0b47bcc17951885368867fb4ee4a79314acb50362a0d5d6adc650761e63cf1b1ae1013db5f5bd3433c7147a324f79c7414097e3a709dbf338db455574886f4d33fb927d5a4a77321d2c93e7d2b0913cd89643a6cd941a438abf821282c63a6d7c26ae7ff3c83f47ca0778192faae50c600f8d45d7b629d89303b2af6005a4d54405a74c9648c5d9699393df20aada9876306c06941734774cd32a8f26dde146f7e3b0e4c83032099709fe69525f5e20978aacd77852648000a61b02abeac3fa7323c9a861d98203fdc895b727d0df7356d0125a9beeb6d8aa65b39480eb2bbb79f240fc20dc16a0d747a614c8cd17a02df5a9a542f608293a852eff01d4da3f7f9dc5613f176ba1cca2a548ac9305e43654397814336102617cca0847ffb0f26c1e696554ca2500d7ef3d429029842cfc0596d7e09ed4a03422ba2eed46238a4ac023f0848a37cd241574a8c067b879ed31578503c5d7f50cd01acda9d107e421d046a4494b93051397922b37beb14ade57d2cebb50ff1730b36005aedfb4cefad27e0c42414a987a5f4076389ebc28e25728a8210b4a335e0c62506b946ac9ce03e1f4849990ccae2bd0856af87fdf2b894b42a6dfc20bda";
		try {
			String result = aes.decryptor(str);
			System.out.println(result);
			XMLSerializer xmlserial = new XMLSerializer();
			JSON json = xmlserial.read(result);
			System.out.println(json.toString());
			JSONObject dataJson = JSONObject.fromObject(json);
			JSONArray body = dataJson.getJSONArray("body");
			System.out.println(body);
			JSONObject manSign = body.getJSONObject(0).getJSONObject("manSign");
			System.out.println(manSign);
			System.out.println(manSign.get("companyCode"));
			String companyCode = manSign.get("companyCode").toString();
			JSONArray manItemSourceList = (JSONArray) body.getJSONObject(0).get("manItemSourceList");
			for(int i=0;i<manItemSourceList.size();i++){
				JSONObject manItemSource = manItemSourceList.getJSONObject(i);
				System.out.println(manItemSource.get("manualId"));
				System.out.println(manItemSource.get("goodsNo"));
			}
			
			StringBuffer sbf = new StringBuffer();
			sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			sbf.append("<mo version=\"1.0.0\">");
			sbf.append("<head>");
			sbf.append("<businessType>RESULT</businessType>");
			sbf.append("</head>");
			sbf.append("<body>");
			sbf.append("<resultInfo>");
			sbf.append("<manResultHead>");
			sbf.append("<companyCode>"+companyCode+"</companyCode>");
			sbf.append("<businessType>MERGER</businessType>");
			sbf.append("<businessNo>业务编号</businessNo>");
			sbf.append("<processTime>"+LogInfo.getCurrentDate()+"</processTime>");
			sbf.append("<processResult>接收成功</processResult>");
			sbf.append("<processComment>处理成功</processComment>");
			sbf.append("</manResultHead>");
			sbf.append("<manResultDetailList>");
			sbf.append("<manResultDetail>");
			sbf.append("<information>处理明细行："+manItemSourceList.size()+"</information>");
			sbf.append("</manResultDetail>");
			sbf.append("</manResultDetailList>");
			sbf.append("</resultInfo>");
			sbf.append("</body>");
			sbf.append("</mo>");
			System.out.println("回执报文：\n"+sbf.toString());
			try {
				result = aes.encrytor(sbf.toString());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
					| UnsupportedEncodingException e) {
				result = "回执报文加密失败";
				e.printStackTrace();
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
