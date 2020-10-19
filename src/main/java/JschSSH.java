import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class JschSSH {
   public static void main(String[] args){
      String username = "test";
      String password = "toor";
      String host = "127.0.0.1";
      int port = 2222;

      JSch jSch = new JSch();

      // 1. 세션 생성
      Session session = null;
      try {
         session = jSch.getSession(username, host, port);
         session.setPassword(password);

         // 2. 세션 설정(호스트 정보를 검색 X)
         java.util.Properties config = new java.util.Properties();
         config.put("StrictHostKeyChecking", "no");
         session.setConfig(config);

         // 3. 연결
         session.connect();
         System.out.println("[*] Session is created " + host);

         // 4. 채널 생성
         Channel channel = session.openChannel("exec");
         ChannelExec channelExec = (ChannelExec) channel;
         System.out.println("[*] Channel is created " + host);

         // 5. 명령어 실행
         channelExec.setCommand("cat /etc/passwd");
         channel.connect();

         // 6. 명령어 실행 결과를 수신
         InputStream inputStream = channel.getInputStream(); // 출력 스트림
         InputStream errStream = ((ChannelExec) channel).getErrStream(); // 에러 스트림

         StringBuilder outputBuffer = new StringBuilder();
         StringBuilder errorBuffer = new StringBuilder();
         byte[] buffer = new byte[1024];

         while(true){
            // 입력 스트림 처리
            while(inputStream.available() > 0){
               int readSize = inputStream.read(buffer, 0, 1024);
               if (readSize < 0) break;
               outputBuffer.append(new String(buffer, 0, readSize));
            }
            // 에러 스트림 처리
            while(errStream.available() > 0){
               int readSize = errStream.read(buffer, 0, 1024);
               if (readSize < 0) break;
               errorBuffer.append(new String(buffer, 0, readSize));
            }
            if(channel.isClosed()){
               // 남아 있는 입력, 에러 처리
               if((inputStream.available()>0) || (errStream.available() > 0)) continue;
               break;
            }
            TimeUnit.MILLISECONDS.sleep(100);
         }

         // 7. 명령어 실행 결과 출력
         System.out.println("output: " + outputBuffer.toString());

         // 연결 종료
         channel.disconnect();
         session.disconnect();
      } catch (JSchException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}