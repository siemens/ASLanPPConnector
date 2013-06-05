// Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.avantssar.aslanpp;

import java.io.IOException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class Debug {

	public static final String LOG_FILE = "aslan.log";
	public static final Logger logger = Logger.getLogger(Debug.class);

	public static enum LogLevel {
		TRACE, DEBUG, WARN, INFO, ERROR, FATAL, OFF
	};

	private static final String LOG_PATTERN = "%d [%5p] - %m%n";

	static {
		PatternLayout layout = new PatternLayout(LOG_PATTERN);
		try {
			RollingFileAppender roll = new RollingFileAppender(layout, LOG_FILE, false);
			roll.setMaxBackupIndex(0);
			roll.setMaxFileSize("100MB");
			roll.setThreshold(Level.ALL);
			Logger.getRootLogger().addAppender(roll);

			// ConsoleAppender console = new ConsoleAppender(layout);
			// console.setThreshold(Level.ALL);
			// Logger.getRootLogger().addAppender(console);

			Logger.getRootLogger().setLevel(Level.ALL);
		}
		catch (IOException e) {
			System.out.println("Failed to initialize logging system.");
			System.out.println("Error message was: " + e.getMessage());
			System.out.println("Defaulting to stdout logging.");
			Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
		}
	}

	public static void initLog(LogLevel level) {
		Logger.getRootLogger().setLevel(mapToRealLogLevel(level));
	}

	private static Level mapToRealLogLevel(LogLevel level) {
		if (level == LogLevel.TRACE) {
			return Level.TRACE;
		}
		else if (level == LogLevel.DEBUG) {
			return Level.DEBUG;
		}
		else if (level == LogLevel.INFO) {
			return Level.INFO;
		}
		else if (level == LogLevel.WARN) {
			return Level.WARN;
		}
		else if (level == LogLevel.ERROR) {
			return Level.ERROR;
		}
		else if (level == LogLevel.FATAL) {
			return Level.FATAL;
		}
		else {
			return Level.OFF;
		}
	}

}
