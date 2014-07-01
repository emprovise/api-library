package com.emprovise.utility;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emprovise.api.campfire.Campfire;
import com.emprovise.api.campfire.Listener;
import com.emprovise.api.campfire.ProxyConfig;
import com.emprovise.api.campfire.Room;
import com.emprovise.api.campfire.models.Message;

public class CampfireUtility {

	private static final Logger logger = LoggerFactory.getLogger(CampfireUtility.class);

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Throwable {
		Option optionDomain = OptionBuilder
				.withLongOpt("domain")
				.hasArg()
				.withDescription("subdomain of campfirenow.com for which campfire account is setup.")
				.isRequired()
				.create("d");
		Option optionUser = OptionBuilder
				.withLongOpt("user")
				.hasArg()
				.withDescription("user name required to access campfire account")
				.isRequired()
				.create("u");
		Option optionPassword = OptionBuilder
				.withLongOpt("password")
				.hasArg()
				.withDescription("password to authenticate the user access to campfire account")
				.isRequired()
				.create("p");
		Option optionRoom = OptionBuilder
				.withLongOpt("room")
				.hasArg()
				.withDescription("room name to join, post, or listen to in campfire.")
				.isRequired()
				.create("r");
		Option optionProxyDomain = OptionBuilder
				.withLongOpt("proxydomain")
				.hasArg()
				.withDescription("proxy domain to connect to subdomain.campfirenow.com over the internet.")
				.create("xd");
		Option optionProxyPort = OptionBuilder
				.withLongOpt("proxyport")
				.hasArg()
				.withType(Number.class)
				.withDescription("proxy port number for proxy domain to connect to campfire over the internet.")
				.create("xt");
		Option optionProxyUser = OptionBuilder
				.withLongOpt("proxyuser")
				.hasArg()
				.withDescription("proxy username to connect to proxy domain.")
				.create("xu");
		Option optionProxyPassword = OptionBuilder
				.withLongOpt("proxypassword")
				.hasArg()
				.withDescription("proxy password for proxy user to connect to proxy domain.")
				.create("xp");
		Option optionPost = OptionBuilder
				.withLongOpt("post")
				.hasArg()
				.withDescription("post will post the argument text to campfire.")
				.create("pt");
		Option optionJoin = OptionBuilder
				.withLongOpt("join")
				.withDescription("join will add the user to the campfire room.")
				.create("jn");
		Option optionListen = OptionBuilder
				.withLongOpt("listen")
				.withDescription("join will add the user to the campfire room.")
				.create("lt");
		Option optionHelp = OptionBuilder
				.withLongOpt("help")
				.withDescription("display help information")
				.create("h");

		Options options = new Options();
		options.addOption(optionDomain);
		options.addOption(optionUser);
		options.addOption(optionPassword);
		options.addOption(optionRoom);
		options.addOption(optionProxyDomain);
		options.addOption(optionProxyPort);
		options.addOption(optionProxyUser);
		options.addOption(optionProxyPassword);
		options.addOption(optionJoin);
		options.addOption(optionPost);
		options.addOption(optionListen);
		options.addOption(optionHelp);

		CommandLine line = null;
		try {
			
			CommandLineParser parser = new BasicParser();
			line = parser.parse(options, args);
			
			if(line.hasOption(optionHelp.getOpt())) {
				displayHelp(options);
				return;
			}
			
			if(!line.hasOption(optionDomain.getOpt())) {
				throw new IllegalArgumentException("campfire subdomain name not provided");
			}
			if(!line.hasOption(optionUser.getOpt())) {
				throw new IllegalArgumentException("campfire user name not provided");
			}
			if(!line.hasOption(optionPassword.getOpt())) {
				throw new IllegalArgumentException("campfire password for user name not provided");
			}
			if(!line.hasOption(optionRoom.getOpt())) {
				throw new IllegalArgumentException("campfire room to access not provided");
			}
			
			String subdomain = line.getOptionValue(optionDomain.getOpt());
			String username = line.getOptionValue(optionUser.getOpt());
			String password = line.getOptionValue(optionPassword.getOpt());

			Campfire campfire = null;
			
			if(line.hasOption(optionProxyDomain.getOpt())) {
				String  proxydomain = line.getOptionValue(optionProxyDomain.getOpt());
				int proxyport = ((Number) line.getParsedOptionValue(optionProxyPort.getOpt())).intValue();
				String  proxyuser = line.getOptionValue(optionProxyUser.getOpt());
				String  proxypassword = line.getOptionValue(optionProxyPassword.getOpt());
				
				ProxyConfig config = new ProxyConfig(proxydomain, proxyport, proxyuser, proxypassword );
				campfire = new Campfire(subdomain, username, password, config);
			}
			else {
				campfire = new Campfire(subdomain, username, password);
			}
			
		    campfire.enableLogging();
		    
			String roomname = line.getOptionValue(optionRoom.getOpt());
		    Room room = getRoom(campfire, roomname);

		    logger.info(String.format("processing login request for username: %s", username));
			
			if(line.hasOption(optionJoin.getOpt())) {
			    System.out.println("joining room " + room);
				room.join();
			}
			
			if(line.hasOption(optionPost.getOpt())) {
				room.join();
				System.out.println("posting '" + line.getOptionValue(optionPost.getOpt()) + "'");
				room.speak(line.getOptionValue(optionPost.getOpt()));
			}
			
			if(line.hasOption(optionListen.getOpt())) {
				room.listen(new Listener() {
					
					@Override
					public void handleNewMessage(Message message) {
					    System.out.println(message.user.name + ": " + message.body);
					}
				});
			}
			
		} catch (MissingOptionException missingex) {
			displayHelp(options);
		} catch (Throwable t) {
			displayHelp(options);
			throw t;
		}
	}

	private static void displayHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar  campfire-api-x.x.x.jar -d subdomain -u user@campfire.com -p password [..args -pt, -lt, -jn]", options);
		logger.info("\n");
	}
	
	private static Room getRoom(Campfire campfire, String roomname) throws IOException {
	    Room room = null;
		List<Room> rooms = campfire.rooms();
		
		for (Room roomIter : rooms) {
			if(roomIter.name.equals(roomname)) {
				room = roomIter;
				break;
			}
		}
		
		return room;
	}
}
