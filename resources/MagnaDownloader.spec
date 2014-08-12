Name:           MagnaDownloader
Version:        1.12
Release:        1%{?dist}
Summary:        A dedicated Downloader for free Music from www.magnatune.com

Group:          Applications/Multimedia
License:        GPL
URL:            http://zen.magnatune.com/
Source0:        http://zen.magnatune.com/MagnaDownloader-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires:  java-1.6.0-openjdk
Requires:       java-1.6.0-openjdk, log4j, jakarta-commons-httpclient

BuildArch:      noarch

%description
This simple GUI will download complete albums from www.magnatune.com.
After pasting the Url of a hifi.m3u file into the GUI, the album with all
its songs will be downloaded to the local filesystem. All doenloaded
songs are previews only.

%prep
%setup -q


%build
mkdir bin
javac -cp .:/usr/share/java/log4j.jar:/usr/share/java/commons-httpclient.jar -d bin `find ./ -name *.java`

mkdir bin/zen/ilgo/music/resources

cp log4j.properties bin
cp zen/ilgo/music/resources/magnatune_16x16.png bin/zen/ilgo/music/resources
cd bin
jar cmfv ../zen/ilgo/music/resources/MANIFEST.MF %{name}.jar ./*

%install
rm -rf $RPM_BUILD_ROOT
install -d -m 755 $RPM_BUILD_ROOT%{_javadir}
install -d -m 755 $RPM_BUILD_ROOT/usr/bin
install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/pixmaps
install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/applications

install -m 644 bin/%{name}.jar $RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar
install -m 755 zen/ilgo/music/resources/magnaDownload $RPM_BUILD_ROOT/usr/bin/magna
echo %{name}-%{version}.jar >> $RPM_BUILD_ROOT/usr/bin/magna
install -m 644 zen/ilgo/music/resources/magnatune_24x24.png $RPM_BUILD_ROOT%{_datadir}/pixmaps/magna.png
install -m 644 zen/ilgo/music/resources/magna.desktop $RPM_BUILD_ROOT%{_datadir}/applications/magna.desktop

desktop-file-install --dir=$RPM_BUILD_ROOT%{_datadir}/applications \
  $RPM_BUILD_ROOT%{_datadir}/applications/magna.desktop

%clean
rm -rf $RPM_BUILD_ROOT

%post
update-desktop-database &> /dev/null || :

%postun
update-desktop-database &> /dev/null || :

%files
%defattr(-,root,root,-)
%{_javadir}/%{name}-%{version}.jar
/usr/bin/magna
%{_datadir}/pixmaps/magna.png
%{_datadir}/applications/magna.desktop

%changelog

* Sat May 30 2009 Roger Holenweger <ilgo711@gmail.com> 1.1
- Synchronized the wgetStateChanged callback method

* Fri May 29 2009 Roger Holenweger <ilgo711@gmail.com> 1.1
- Added Drag and Drop, removed "Add Album" button

* Wed May 27 2009 Roger Holenweger <ilgo711@gmail.com> 1.0
- Initial MagnaDownloader
