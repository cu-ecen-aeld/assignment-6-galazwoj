# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
#
# i. Use devtool add to add your assignment 3 aesdchar kernel mnodule repository to your build using a recipe name aesd-char
#	~/github/assignment-6-galazwoj$ 	source poky/oe-init-build-env
#	~/github/assignment-6-galazwoj/build$ 	devtool add aesd-char https://github.com/cu-ecen-aeld/assignments-3-and-later-galazwoj
# ii. Use devtool finish to save the corresponding .bb and Makefile patch files to your repository as a "aesd-char" recipe in the meta-aesd layer.
#	~/github/assignment-6-galazwoj/build$ 	devtool finish -f aesd-char meta-aesd
# iii. Modify the task-install in the .bb file to use the correct module folder for the M argument.  
# 	You can do this with an EXTRA_OEMAKE_append_task-install = " -C ${STAGING_KERNEL_DIR} M=${S}/aesd-char-driver"
# iv. Add a "files" subfolder in your scull recipe and place an init script in this folder which does the portion of initialization specific to the scull module.
#	placed S95_aesdchar_module there	
# v. Use the update-rc.d framework used in the previous assignment to install the script.
# vi. Include aesd-char in your core-image-aesd image.
#
# based on meta-ased/recipes-aesd-assignments/aesd-assignments/aesd-assignments_git.bb
#
# See https://git.yoctoproject.org/poky/tree/meta/files/common-licenses
SUMMARY = "aesd-char module"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# XTODO: Set this  with the path to your assignments rep.  Use ssh protocol and see lecture notes
# about how to setup ssh-agent for passwordless access
SRC_URI  = "git://github.com/cu-ecen-aeld/assignments-3-and-later-galazwoj;protocol=https;branch=master"
SRC_URI += "file://S85_aesdchar_module" 
	 
# Modify these as desired
PV = "1.0+git${SRCPV}"
# XTODO: set to reference a specific commit hash in your assignment repo
SRCREV = "f63943290c268168800728c04429926360edf10a"

# This sets your staging directory based on WORKDIR, where WORKDIR is defined at 
# https://docs.yoctoproject.org/ref-manual/variables.html?highlight=workdir#term-WORKDIR
# We reference the "scull" directory here to build from the "scull" directory
# in your assignments repo
S = "${WORKDIR}/git/aesd-char-driver"

inherit module
# (see iii above)
EXTRA_OEMAKE:append:task-install = " -C ${STAGING_KERNEL_DIR} M=${S}"
EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_DIR}"
RPROVIDES:${PN} += "kernel-module-aesdchar"

# (see iv above)
# Picked up from
# https://github.com/sutajiokousagi/meta-chumby/blob/master/recipes/examples/init-example_1.0.bb
#
# Inherit the update-rc.d.bbclass file located in openembedded/classes/.
# This will take care of setting up startup links when the package is
# installed.
inherit update-rc.d

# Tell the update-rc.d package which program will be used as the startup script.
# The script will be called with the "start" command at system
# startup, the "stop" command at system shutdown, and the "restart" command
# when the package is updated.
INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME:${PN} = "S85_aesdchar_module"

# Run the command at step 90 during startup, and step 10 during shutdown.
# Because the numbers go from 00 - 99, larger numbers will be run later on.
# If your program is required for system startup, put a low number here.
# If it's user-facing or less critical for system startup, put a higher
# number here.
# As a tradition, the shutdown number should be 100-startup_number.  That
# way scripts are stopped in the reverse order they were started in.  Since
# our startup number here is 90, the shutdown number will be 100-90 or 10.
INITSCRIPT_PARAMS = "defaults 85 15"

FILES:${PN} += "${INIT_D_DIR}/${INITSCRIPT_NAME:${PN}}"
# https://stackoverflow.com/questions/49748528/yocto-files-directories-were-installed-but-not-shipped-in-any-package
FILES:${PN} += "${bindir}/aesdchar_load"     
FILES:${PN} += "${bindir}/aesdchar_unload"   

do_install () {
	# TODO: Install your binaries/scripts here.
	# Be sure to install the target directory with install -d first
	# Yocto variables ${D} and ${S} are useful here, which you can read about at 
	# https://docs.yoctoproject.org/ref-manual/variables.html?highlight=workdir#term-D
	# and
	# https://docs.yoctoproject.org/ref-manual/variables.html?highlight=workdir#term-S
	# See example at https://github.com/cu-ecen-aeld/ecen5013-yocto/blob/ecen5013-hello-world/meta-ecen5013/recipes-ecen5013/ecen5013-hello-world/ecen5013-hello-world_git.bb

#	script to /etc/init.d	
	install -d ${D}${INIT_D_DIR}
	install -m 0755 ${WORKDIR}/${INITSCRIPT_NAME:${PN}} ${D}${INIT_D_DIR}

#	kernel module
	install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra
	install -m 755 ${S}/aesdchar.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra/aesdchar.ko

#	scripts to /usr/bin
	install -d ${D}${bindir}
	install -m 755 ${S}/aesdchar_load   ${D}${bindir}/
	install -m 755 ${S}/aesdchar_unload ${D}${bindir}/
}

