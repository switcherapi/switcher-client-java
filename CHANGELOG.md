# Change Log

- 1.3.4:
	- Added support to default values for the environment properties file
	- Updated org.junit.jupiter:junit-jupiter-api from 5.8.1 to 5.8.2
	- Updated org.apache.logging.log4j:log4j-core from 2.17.0 to 2.17.1
	- Updated managed dependency jackson@2.12.2 to use 2.13.1; fix vulnerability caused by jersey 2.35
- 1.3.3:
	- Updated dependency org.apache.logging.log4j from 2.15.0 to 2.17.0
	- Updated managed dependency junit to 4.13.1
- 1.3.0:
	- Optimized Switcher instance creation management
	- Added Throttling and Async calls
	- Updated com.google.code.gson:gson from 2.8.6 to 2.8.9
	- Updated Jersey dependencies from 2.34 to 2.35
	- Fixed Autoload snapshot is creating null as file name
- 1.2.1: Medium Severity Security Patch: Jersey has been updated - 2.33 to 2.34
- 1.2.0:
	- Changed how SwitcherContext is implemented - added support to properties file
	- Offline mode can programmatically load snapshots
	- Added extra security layer for verifying features
	- Added @SwitcherMock feature
	- Smoke testing
	- Removed PowerMockito: tests are way simpler to read using Okhttp3
	- Updated dependecy junit to JUnit5-jupiter
- 1.1.0:
	- Improved snapshot lookup mechanism
	- Both online and offline modes can validate/update snapshot version
- 1.0.10:
	- Dependency patch: Commons Net from 3.7.1 to 3.7.2
	- Critical Fix: Downgraded jersey-media-json-jackson 3.0.0 to 2.33
- 1.0.9: Security patch
	- Updated dependency jersey-client from 2.31 to 2.32
	- Updated dependency jersey-hk2 from 2.31 to 2.32
	- Updated dependency jersey-media-json-jackson from 2.31 to 3.0.0
	- Updated dependency common-net from 3.7 to 3.7.1
- 1.0.8:
	- Fixed issues when using Silent Mode
	- Fixed error when using only access to online API
	- Improved validation when verifying whether API is accessible
	- Added validations when preparing the Switcher Context
	- Updated dependency commons-net.version from 3.6 to 3.7
- 1.0.7: Added Regex Validation
- 1.0.6: Updated depencencies & new features
	- Updated dependency jersey-hk2 from 2.28 to 2.31
	- Updated dependency commons-net from 3.3 to 3.6
	- Updated dependency commons-lang3 from 3.8.1 to 3.10
	- Updated dependency gson from 2.8.5 to 2.8.6
	- Added execution log to Switcher
	- Added bypass metrics and show detailed criteria evaluation options to Switcher objects
- 1.0.5: Security patch - Jersey has been updated - 2.28 to 2.31
- 1.0.4: Added Numeric Validation
- 1.0.3: Security patch - Log4J has been updated - 2.13.1 to 2.13.3
- 1.0.2: 
    - Improved performance when loading snapshot file.
    - Snapshot file auto load when updated.
    - Re-worked built-in mock implementation
- 1.0.1: Security patch - Log4J has been updated
- 1.0.0: Working release