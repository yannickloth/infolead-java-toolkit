package eu.infolead.jtk.time;

import java.time.ZoneId;
import java.util.TimeZone;

public enum TZ {
    AFRICA_ABIDJAN("Africa/Abidjan"),
    AFRICA_ACCRA("Africa/Accra"),
    AFRICA_ADDIS_ABABA("Africa/Addis_Ababa"),
    AFRICA_ALGIERS("Africa/Algiers"),
    AFRICA_ASMARA("Africa/Asmara"),
    AFRICA_ASMERA("Africa/Asmera"),
    AFRICA_BAMAKO("Africa/Bamako"),
    AFRICA_BANGUI("Africa/Bangui"),
    AFRICA_BANJUL("Africa/Banjul"),
    AFRICA_BISSAU("Africa/Bissau"),
    AFRICA_BLANTYRE("Africa/Blantyre"),
    AFRICA_BRAZZAVILLE("Africa/Brazzaville"),
    AFRICA_BUJUMBURA("Africa/Bujumbura"),
    AFRICA_CAIRO("Africa/Cairo"),
    AFRICA_CASABLANCA("Africa/Casablanca"),
    AFRICA_CEUTA("Africa/Ceuta"),
    AFRICA_CONAKRY("Africa/Conakry"),
    AFRICA_DAKAR("Africa/Dakar"),
    AFRICA_DAR_ES_SALAAM("Africa/Dar_es_Salaam"),
    AFRICA_DJIBOUTI("Africa/Djibouti"),
    AFRICA_DOUALA("Africa/Douala"),
    AFRICA_EL_AAIUN("Africa/El_Aaiun"),
    AFRICA_FREETOWN("Africa/Freetown"),
    AFRICA_GABORONE("Africa/Gaborone"),
    AFRICA_HARARE("Africa/Harare"),
    AFRICA_JOHANNESBURG("Africa/Johannesburg"),
    AFRICA_JUBA("Africa/Juba"),
    AFRICA_KAMPALA("Africa/Kampala"),
    AFRICA_KHARTOUM("Africa/Khartoum"),
    AFRICA_KIGALI("Africa/Kigali"),
    AFRICA_KINSHASA("Africa/Kinshasa"),
    AFRICA_LAGOS("Africa/Lagos"),
    AFRICA_LIBREVILLE("Africa/Libreville"),
    AFRICA_LOME("Africa/Lome"),
    AFRICA_LUANDA("Africa/Luanda"),
    AFRICA_LUBUMBASHI("Africa/Lubumbashi"),
    AFRICA_LUSAKA("Africa/Lusaka"),
    AFRICA_MALABO("Africa/Malabo"),
    AFRICA_MAPUTO("Africa/Maputo"),
    AFRICA_MASERU("Africa/Maseru"),
    AFRICA_MBABANE("Africa/Mbabane"),
    AFRICA_MOGADISHU("Africa/Mogadishu"),
    AFRICA_MONROVIA("Africa/Monrovia"),
    AFRICA_NAIROBI("Africa/Nairobi"),
    AFRICA_NDJAMENA("Africa/Ndjamena"),
    AFRICA_NIAMEY("Africa/Niamey"),
    AFRICA_NOUAKCHOTT("Africa/Nouakchott"),
    AFRICA_OUAGADOUGOU("Africa/Ouagadougou"),
    AFRICA_PORTO_NOVO("Africa/Porto-Novo"),
    AFRICA_SAO_TOME("Africa/Sao_Tome"),
    AFRICA_TIMBUKTU("Africa/Timbuktu"),
    AFRICA_TRIPOLI("Africa/Tripoli"),
    AFRICA_TUNIS("Africa/Tunis"),
    AFRICA_WINDHOEK("Africa/Windhoek"),
    AMERICA_ADAK("America/Adak"),
    AMERICA_ANCHORAGE("America/Anchorage"),
    AMERICA_ANGUILLA("America/Anguilla"),
    AMERICA_ANTIGUA("America/Antigua"),
    AMERICA_ARAGUAINA("America/Araguaina"),
    AMERICA_ARGENTINA("America/Argentina/Catamarca"),
    AMERICA_ARGENTINA_BUENOS_AIRES("America/Argentina/Buenos_Aires"),
    AMERICA_ARGENTINA_COMOD_RIVADAVIA("America/Argentina/ComodRivadavia"),
    AMERICA_ARGENTINA_CORDOBA("America/Argentina/Cordoba"),
    AMERICA_ARGENTINA_JUJUY("America/Argentina/Jujuy"),
    AMERICA_ARGENTINA_LA_RIOJA("America/Argentina/La_Rioja"),
    AMERICA_ARGENTINA_MENDOZA("America/Argentina/Mendoza"),
    AMERICA_ARGENTINA_RIO_GALLEGOS("America/Argentina/Rio_Gallegos"),
    AMERICA_ARGENTINA_SALTA("America/Argentina/Salta"),
    AMERICA_ARGENTINA_SAN_JUAN("America/Argentina/San_Juan"),
    AMERICA_ARGENTINA_SAN_LUIS("America/Argentina/San_Luis"),
    AMERICA_ARGENTINA_TUCUMAN("America/Argentina/Tucuman"),
    AMERICA_ARGENTINA_USHUAIA("America/Argentina/Ushuaia"),
    AMERICA_ARUBA("America/Aruba"),
    AMERICA_ASUNCION("America/Asuncion"),
    AMERICA_ATIKOKAN("America/Atikokan"),
    AMERICA_ATKA("America/Atka"),
    AMERICA_BAHIA("America/Bahia"),
    AMERICA_BAHIA_BANDERAS("America/Bahia_Banderas"),
    AMERICA_BARBADOS("America/Barbados"),
    AMERICA_BELEM("America/Belem"),
    AMERICA_BELIZE("America/Belize"),
    AMERICA_BLANC_SABLON("America/Blanc-Sablon"),
    AMERICA_BOA_VISTA("America/Boa_Vista"),
    AMERICA_BOGOTA("America/Bogota"),
    AMERICA_BOISE("America/Boise"),
    AMERICA_BUENOS_AIRES("America/Buenos_Aires"),
    AMERICA_CAMBRIDGE_BAY("America/Cambridge_Bay"),
    AMERICA_CAMPO_GRANDE("America/Campo_Grande"),
    AMERICA_CANCUN("America/Cancun"),
    AMERICA_CARACAS("America/Caracas"),
    AMERICA_CATAMARCA("America/Catamarca"),
    AMERICA_CAYENNE("America/Cayenne"),
    AMERICA_CAYMAN("America/Cayman"),
    AMERICA_CHICAGO("America/Chicago"),
    AMERICA_CHIHUAHUA("America/Chihuahua"),
    AMERICA_CORAL_HARBOUR("America/Coral_Harbour"),
    AMERICA_CORDOBA("America/Cordoba"),
    AMERICA_COSTA_RICA("America/Costa_Rica"),
    AMERICA_CRESTON("America/Creston"),
    AMERICA_CUIABA("America/Cuiaba"),
    AMERICA_CURACAO("America/Curacao"),
    AMERICA_DANMARKSHAVN("America/Danmarkshavn"),
    AMERICA_DAWSON("America/Dawson"),
    AMERICA_DAWSON_CREEK("America/Dawson_Creek"),
    AMERICA_DENVER("America/Denver"),
    AMERICA_DETROIT("America/Detroit"),
    AMERICA_DOMINICA("America/Dominica"),
    AMERICA_EDMONTON("America/Edmonton"),
    AMERICA_EIRUNEPE("America/Eirunepe"),
    AMERICA_EL_SALVADOR("America/El_Salvador"),
    AMERICA_ENSENADA("America/Ensenada"),
    AMERICA_FORTALEZA("America/Fortaleza"),
    AMERICA_FORT_NELSON("America/Fort_Nelson"),
    AMERICA_FORT_WAYNE("America/Fort_Wayne"),
    AMERICA_GLACE_BAY("America/Glace_Bay"),
    AMERICA_GODTHAB("America/Godthab"),
    AMERICA_GOOSE_BAY("America/Goose_Bay"),
    AMERICA_GRAND_TURK("America/Grand_Turk"),
    AMERICA_GRENADA("America/Grenada"),
    AMERICA_GUADELOUPE("America/Guadeloupe"),
    AMERICA_GUATEMALA("America/Guatemala"),
    AMERICA_GUAYAQUIL("America/Guayaquil"),
    AMERICA_GUYANA("America/Guyana"),
    AMERICA_HALIFAX("America/Halifax"),
    AMERICA_HAVANA("America/Havana"),
    AMERICA_HERMOSILLO("America/Hermosillo"),
    AMERICA_INDIANA("America/Indiana/Petersburg"),
    AMERICA_INDIANAPOLIS("America/Indianapolis"),
    AMERICA_INDIANA_INDIANAPOLIS("America/Indiana/Indianapolis"),
    AMERICA_INDIANA_KNOX("America/Indiana/Knox"),
    AMERICA_INDIANA_MARENGO("America/Indiana/Marengo"),
    AMERICA_INDIANA_TELL_CITY("America/Indiana/Tell_City"),
    AMERICA_INDIANA_VEVAY("America/Indiana/Vevay"),
    AMERICA_INDIANA_VINCENNES("America/Indiana/Vincennes"),
    AMERICA_INDIANA_WINAMAC("America/Indiana/Winamac"),
    AMERICA_INUVIK("America/Inuvik"),
    AMERICA_IQALUIT("America/Iqaluit"),
    AMERICA_JAMAICA("America/Jamaica"),
    AMERICA_JUJUY("America/Jujuy"),
    AMERICA_JUNEAU("America/Juneau"),
    AMERICA_KENTUCKY("America/Kentucky/Louisville"),
    AMERICA_KENTUCKY_MONTICELLO("America/Kentucky/Monticello"),
    AMERICA_KNOX_IN("America/Knox_IN"),
    AMERICA_KRALENDIJK("America/Kralendijk"),
    AMERICA_LA_PAZ("America/La_Paz"),
    AMERICA_LIMA("America/Lima"),
    AMERICA_LOS_ANGELES("America/Los_Angeles"),
    AMERICA_LOUISVILLE("America/Louisville"),
    AMERICA_LOWER_PRINCES("America/Lower_Princes"),
    AMERICA_MACEIO("America/Maceio"),
    AMERICA_MANAGUA("America/Managua"),
    AMERICA_MANAUS("America/Manaus"),
    AMERICA_MARIGOT("America/Marigot"),
    AMERICA_MARTINIQUE("America/Martinique"),
    AMERICA_MATAMOROS("America/Matamoros"),
    AMERICA_MAZATLAN("America/Mazatlan"),
    AMERICA_MENDOZA("America/Mendoza"),
    AMERICA_MENOMINEE("America/Menominee"),
    AMERICA_MERIDA("America/Merida"),
    AMERICA_METLAKATLA("America/Metlakatla"),
    AMERICA_MEXICO_CITY("America/Mexico_City"),
    AMERICA_MIQUELON("America/Miquelon"),
    AMERICA_MONCTON("America/Moncton"),
    AMERICA_MONTERREY("America/Monterrey"),
    AMERICA_MONTEVIDEO("America/Montevideo"),
    AMERICA_MONTREAL("America/Montreal"),
    AMERICA_MONTSERRAT("America/Montserrat"),
    AMERICA_NASSAU("America/Nassau"),
    AMERICA_NEW_YORK("America/New_York"),
    AMERICA_NIPIGON("America/Nipigon"),
    AMERICA_NOME("America/Nome"),
    AMERICA_NORONHA("America/Noronha"),
    AMERICA_NORTH_DAKOTA("America/North_Dakota/Beulah"),
    AMERICA_NORTH_DAKOTA_CENTER("America/North_Dakota/Center"),
    AMERICA_NORTH_DAKOTA_NEW_SALEM("America/North_Dakota/New_Salem"),
    AMERICA_NUUK("America/Nuuk"),
    AMERICA_OJINAGA("America/Ojinaga"),
    AMERICA_PANAMA("America/Panama"),
    AMERICA_PANGNIRTUNG("America/Pangnirtung"),
    AMERICA_PARAMARIBO("America/Paramaribo"),
    AMERICA_PHOENIX("America/Phoenix"),
    AMERICA_PORTO_ACRE("America/Porto_Acre"),
    AMERICA_PORTO_VELHO("America/Porto_Velho"),
    AMERICA_PORT_AU_PRINCE("America/Port-au-Prince"),
    AMERICA_PORT_OF_SPAIN("America/Port_of_Spain"),
    AMERICA_PUERTO_RICO("America/Puerto_Rico"),
    AMERICA_PUNTA_ARENAS("America/Punta_Arenas"),
    AMERICA_RAINY_RIVER("America/Rainy_River"),
    AMERICA_RANKIN_INLET("America/Rankin_Inlet"),
    AMERICA_RECIFE("America/Recife"),
    AMERICA_REGINA("America/Regina"),
    AMERICA_RESOLUTE("America/Resolute"),
    AMERICA_RIO_BRANCO("America/Rio_Branco"),
    AMERICA_ROSARIO("America/Rosario"),
    AMERICA_SANTAREM("America/Santarem"),
    AMERICA_SANTA_ISABEL("America/Santa_Isabel"),
    AMERICA_SANTIAGO("America/Santiago"),
    AMERICA_SANTO_DOMINGO("America/Santo_Domingo"),
    AMERICA_SAO_PAULO("America/Sao_Paulo"),
    AMERICA_SCORESBYSUND("America/Scoresbysund"),
    AMERICA_SHIPROCK("America/Shiprock"),
    AMERICA_SITKA("America/Sitka"),
    AMERICA_ST_BARTHELEMY("America/St_Barthelemy"),
    AMERICA_ST_JOHNS("America/St_Johns"),
    AMERICA_ST_KITTS("America/St_Kitts"),
    AMERICA_ST_LUCIA("America/St_Lucia"),
    AMERICA_ST_THOMAS("America/St_Thomas"),
    AMERICA_ST_VINCENT("America/St_Vincent"),
    AMERICA_SWIFT_CURRENT("America/Swift_Current"),
    AMERICA_TEGUCIGALPA("America/Tegucigalpa"),
    AMERICA_THULE("America/Thule"),
    AMERICA_THUNDER_BAY("America/Thunder_Bay"),
    AMERICA_TIJUANA("America/Tijuana"),
    AMERICA_TORONTO("America/Toronto"),
    AMERICA_TORTOLA("America/Tortola"),
    AMERICA_VANCOUVER("America/Vancouver"),
    AMERICA_VIRGIN("America/Virgin"),
    AMERICA_WHITEHORSE("America/Whitehorse"),
    AMERICA_WINNIPEG("America/Winnipeg"),
    AMERICA_YAKUTAT("America/Yakutat"),
    AMERICA_YELLOWKNIFE("America/Yellowknife"),
    ANTARCTICA_CASEY("Antarctica/Casey"),
    ANTARCTICA_DAVIS("Antarctica/Davis"),
    ANTARCTICA_DUMONTDURVILLE("Antarctica/DumontDUrville"),
    ANTARCTICA_MACQUARIE("Antarctica/Macquarie"),
    ANTARCTICA_MAWSON("Antarctica/Mawson"),
    ANTARCTICA_MCMURDO("Antarctica/McMurdo"),
    ANTARCTICA_PALMER("Antarctica/Palmer"),
    ANTARCTICA_ROTHERA("Antarctica/Rothera"),
    ANTARCTICA_SOUTH_POLE("Antarctica/South_Pole"),
    ANTARCTICA_SYOWA("Antarctica/Syowa"),
    ANTARCTICA_TROLL("Antarctica/Troll"),
    ANTARCTICA_VOSTOK("Antarctica/Vostok"),
    ARCTIC_LONGYEARBYEN("Arctic/Longyearbyen"),
    ASIA_ADEN("Asia/Aden"),
    ASIA_ALMATY("Asia/Almaty"),
    ASIA_AMMAN("Asia/Amman"),
    ASIA_ANADYR("Asia/Anadyr"),
    ASIA_AQTAU("Asia/Aqtau"),
    ASIA_AQTOBE("Asia/Aqtobe"),
    ASIA_ASHGABAT("Asia/Ashgabat"),
    ASIA_ASHKHABAD("Asia/Ashkhabad"),
    ASIA_ATYRAU("Asia/Atyrau"),
    ASIA_BAGHDAD("Asia/Baghdad"),
    ASIA_BAHRAIN("Asia/Bahrain"),
    ASIA_BAKU("Asia/Baku"),
    ASIA_BANGKOK("Asia/Bangkok"),
    ASIA_BARNAUL("Asia/Barnaul"),
    ASIA_BEIRUT("Asia/Beirut"),
    ASIA_BISHKEK("Asia/Bishkek"),
    ASIA_BRUNEI("Asia/Brunei"),
    ASIA_CALCUTTA("Asia/Calcutta"),
    ASIA_CHITA("Asia/Chita"),
    ASIA_CHOIBALSAN("Asia/Choibalsan"),
    ASIA_CHONGQING("Asia/Chongqing"),
    ASIA_CHUNGKING("Asia/Chungking"),
    ASIA_COLOMBO("Asia/Colombo"),
    ASIA_DACCA("Asia/Dacca"),
    ASIA_DAMASCUS("Asia/Damascus"),
    ASIA_DHAKA("Asia/Dhaka"),
    ASIA_DILI("Asia/Dili"),
    ASIA_DUBAI("Asia/Dubai"),
    ASIA_DUSHANBE("Asia/Dushanbe"),
    ASIA_FAMAGUSTA("Asia/Famagusta"),
    ASIA_GAZA("Asia/Gaza"),
    ASIA_HARBIN("Asia/Harbin"),
    ASIA_HEBRON("Asia/Hebron"),
    ASIA_HONG_KONG("Asia/Hong_Kong"),
    ASIA_HOVD("Asia/Hovd"),
    ASIA_HO_CHI_MINH("Asia/Ho_Chi_Minh"),
    ASIA_IRKUTSK("Asia/Irkutsk"),
    ASIA_ISTANBUL("Asia/Istanbul"),
    ASIA_JAKARTA("Asia/Jakarta"),
    ASIA_JAYAPURA("Asia/Jayapura"),
    ASIA_JERUSALEM("Asia/Jerusalem"),
    ASIA_KABUL("Asia/Kabul"),
    ASIA_KAMCHATKA("Asia/Kamchatka"),
    ASIA_KARACHI("Asia/Karachi"),
    ASIA_KASHGAR("Asia/Kashgar"),
    ASIA_KATHMANDU("Asia/Kathmandu"),
    ASIA_KATMANDU("Asia/Katmandu"),
    ASIA_KHANDYGA("Asia/Khandyga"),
    ASIA_KOLKATA("Asia/Kolkata"),
    ASIA_KRASNOYARSK("Asia/Krasnoyarsk"),
    ASIA_KUALA_LUMPUR("Asia/Kuala_Lumpur"),
    ASIA_KUCHING("Asia/Kuching"),
    ASIA_KUWAIT("Asia/Kuwait"),
    ASIA_MACAO("Asia/Macao"),
    ASIA_MACAU("Asia/Macau"),
    ASIA_MAGADAN("Asia/Magadan"),
    ASIA_MAKASSAR("Asia/Makassar"),
    ASIA_MANILA("Asia/Manila"),
    ASIA_MUSCAT("Asia/Muscat"),
    ASIA_NICOSIA("Asia/Nicosia"),
    ASIA_NOVOKUZNETSK("Asia/Novokuznetsk"),
    ASIA_NOVOSIBIRSK("Asia/Novosibirsk"),
    ASIA_OMSK("Asia/Omsk"),
    ASIA_ORAL("Asia/Oral"),
    ASIA_PHNOM_PENH("Asia/Phnom_Penh"),
    ASIA_PONTIANAK("Asia/Pontianak"),
    ASIA_PYONGYANG("Asia/Pyongyang"),
    ASIA_QATAR("Asia/Qatar"),
    ASIA_QOSTANAY("Asia/Qostanay"),
    ASIA_QYZYLORDA("Asia/Qyzylorda"),
    ASIA_RANGOON("Asia/Rangoon"),
    ASIA_RIYADH("Asia/Riyadh"),
    ASIA_SAIGON("Asia/Saigon"),
    ASIA_SAKHALIN("Asia/Sakhalin"),
    ASIA_SAMARKAND("Asia/Samarkand"),
    ASIA_SEOUL("Asia/Seoul"),
    ASIA_SHANGHAI("Asia/Shanghai"),
    ASIA_SINGAPORE("Asia/Singapore"),
    ASIA_SREDNEKOLYMSK("Asia/Srednekolymsk"),
    ASIA_TAIPEI("Asia/Taipei"),
    ASIA_TASHKENT("Asia/Tashkent"),
    ASIA_TBILISI("Asia/Tbilisi"),
    ASIA_TEHRAN("Asia/Tehran"),
    ASIA_TEL_AVIV("Asia/Tel_Aviv"),
    ASIA_THIMBU("Asia/Thimbu"),
    ASIA_THIMPHU("Asia/Thimphu"),
    ASIA_TOKYO("Asia/Tokyo"),
    ASIA_TOMSK("Asia/Tomsk"),
    ASIA_UJUNG_PANDANG("Asia/Ujung_Pandang"),
    ASIA_ULAANBAATAR("Asia/Ulaanbaatar"),
    ASIA_ULAN_BATOR("Asia/Ulan_Bator"),
    ASIA_URUMQI("Asia/Urumqi"),
    ASIA_USTNNERA("Asia/Ust-Nera"),
    ASIA_VIENTIANE("Asia/Vientiane"),
    ASIA_VLADIVOSTOK("Asia/Vladivostok"),
    ASIA_YAKUTSK("Asia/Yakutsk"),
    ASIA_YANGON("Asia/Yangon"),
    ASIA_YEKATERINBURG("Asia/Yekaterinburg"),
    ASIA_YEREVAN("Asia/Yerevan"),
    ATLANTIC_AZORES("Atlantic/Azores"),
    ATLANTIC_BERMUDA("Atlantic/Bermuda"),
    ATLANTIC_CANARY("Atlantic/Canary"),
    ATLANTIC_CAPE_VERDE("Atlantic/Cape_Verde"),
    ATLANTIC_FAEROE("Atlantic/Faeroe"),
    ATLANTIC_FAROE("Atlantic/Faroe"),
    ATLANTIC_JAN_MAYEN("Atlantic/Jan_Mayen"),
    ATLANTIC_MADEIRA("Atlantic/Madeira"),
    ATLANTIC_REYKJAVIK("Atlantic/Reykjavik"),
    ATLANTIC_SOUTH_GEORGIA("Atlantic/South_Georgia"),
    ATLANTIC_STANLEY("Atlantic/Stanley"),
    ATLANTIC_ST_HELENA("Atlantic/St_Helena"),
    AUSTRALIA_ACT("Australia/ACT"),
    AUSTRALIA_ADELAIDE("Australia/Adelaide"),
    AUSTRALIA_BRISBANE("Australia/Brisbane"),
    AUSTRALIA_BROKEN_HILL("Australia/Broken_Hill"),
    AUSTRALIA_CANBERRA("Australia/Canberra"),
    AUSTRALIA_CURRIE("Australia/Currie"),
    AUSTRALIA_DARWIN("Australia/Darwin"),
    AUSTRALIA_EUCLA("Australia/Eucla"),
    AUSTRALIA_HOBART("Australia/Hobart"),
    AUSTRALIA_LHI("Australia/LHI"),
    AUSTRALIA_LINDEMAN("Australia/Lindeman"),
    AUSTRALIA_LORD_HOWE("Australia/Lord_Howe"),
    AUSTRALIA_MELBOURNE("Australia/Melbourne"),
    AUSTRALIA_NORTH("Australia/North"),
    AUSTRALIA_NSW("Australia/NSW"),
    AUSTRALIA_PERTH("Australia/Perth"),
    AUSTRALIA_QUEENSLAND("Australia/Queensland"),
    AUSTRALIA_SOUTH("Australia/South"),
    AUSTRALIA_SYDNEY("Australia/Sydney"),
    AUSTRALIA_TASMANIA("Australia/Tasmania"),
    AUSTRALIA_VICTORIA("Australia/Victoria"),
    AUSTRALIA_WEST("Australia/West"),
    AUSTRALIA_YANCOWINNA("Australia/Yancowinna"),
    BRAZIL_ACRE("Brazil/Acre"),
    BRAZIL_DENORONHA("Brazil/DeNoronha"),
    BRAZIL_EAST("Brazil/East"),
    BRAZIL_WEST("Brazil/West"),
    CANADA_ATLANTIC("Canada/Atlantic"),
    CANADA_CENTRAL("Canada/Central"),
    CANADA_EASTERN("Canada/Eastern"),
    CANADA_MOUNTAIN("Canada/Mountain"),
    CANADA_NEWFOUNDLAND("Canada/Newfoundland"),
    CANADA_PACIFIC("Canada/Pacific"),
    CANADA_SASKATCHEWAN("Canada/Saskatchewan"),
    CANADA_YUKON("Canada/Yukon"),
    CET("CET"),
    CHILE_CONTINENTAL("Chile/Continental"),
    CHILE_EASTERISLAND("Chile/EasterIsland"),
    CST6CDT("CST6CDT"),
    CUBA("Cuba"),
    EET("EET"),
    EGYPT("Egypt"),
    EIRE("Eire"),
    EST5EDT("EST5EDT"),
    ETC_GMT("Etc/GMT"),
    ETC_GMT0("Etc/GMT0"),
    ETC_GMT_MINUS0("Etc/GMT_MINUS0"),
    ETC_GMT_MINUS1("Etc/GMT_MINUS1"),
    ETC_GMT_MINUS10("Etc/GMT_MINUS10"),
    ETC_GMT_MINUS11("Etc/GMT_MINUS11"),
    ETC_GMT_MINUS12("Etc/GMT_MINUS12"),
    ETC_GMT_MINUS13("Etc/GMT_MINUS13"),
    ETC_GMT_MINUS14("Etc/GMT_MINUS14"),
    ETC_GMT_MINUS2("Etc/GMT_MINUS2"),
    ETC_GMT_MINUS3("Etc/GMT_MINUS3"),
    ETC_GMT_MINUS4("Etc/GMT_MINUS4"),
    ETC_GMT_MINUS5("Etc/GMT_MINUS5"),
    ETC_GMT_MINUS6("Etc/GMT_MINUS6"),
    ETC_GMT_MINUS7("Etc/GMT_MINUS7"),
    ETC_GMT_MINUS8("Etc/GMT_MINUS8"),
    ETC_GMT_MINUS9("Etc/GMT_MINUS9"),
    ETC_GMT_PLUS0("Etc/GMT_PLUS0"),
    ETC_GMT_PLUS1("Etc/GMT_PLUS1"),
    ETC_GMT_PLUS10("Etc/GMT_PLUS10"),
    ETC_GMT_PLUS11("Etc/GMT_PLUS11"),
    ETC_GMT_PLUS12("Etc/GMT_PLUS12"),
    ETC_GMT_PLUS2("Etc/GMT_PLUS2"),
    ETC_GMT_PLUS3("Etc/GMT_PLUS3"),
    ETC_GMT_PLUS4("Etc/GMT_PLUS4"),
    ETC_GMT_PLUS5("Etc/GMT_PLUS5"),
    ETC_GMT_PLUS6("Etc/GMT_PLUS6"),
    ETC_GMT_PLUS7("Etc/GMT_PLUS7"),
    ETC_GMT_PLUS8("Etc/GMT_PLUS8"),
    ETC_GMT_PLUS9("Etc/GMT_PLUS9"),
    ETC_GREENWICH("Etc/Greenwich"),
    ETC_UCT("Etc/UCT"),
    ETC_UNIVERSAL("Etc/Universal"),
    ETC_UTC("Etc/UTC"),
    ETC_ZULU("Etc/Zulu"),
    EUROPE_AMSTERDAM("Europe/Amsterdam"),
    EUROPE_ANDORRA("Europe/Andorra"),
    EUROPE_ASTRAKHAN("Europe/Astrakhan"),
    EUROPE_ATHENS("Europe/Athens"),
    EUROPE_BELFAST("Europe/Belfast"),
    EUROPE_BELGRADE("Europe/Belgrade"),
    EUROPE_BERLIN("Europe/Berlin"),
    EUROPE_BRATISLAVA("Europe/Bratislava"),
    EUROPE_BRUSSELS("Europe/Brussels"),
    EUROPE_BUCHAREST("Europe/Bucharest"),
    EUROPE_BUDAPEST("Europe/Budapest"),
    EUROPE_BUSINGEN("Europe/Busingen"),
    EUROPE_CHISINAU("Europe/Chisinau"),
    EUROPE_COPENHAGEN("Europe/Copenhagen"),
    EUROPE_DUBLIN("Europe/Dublin"),
    EUROPE_GIBRALTAR("Europe/Gibraltar"),
    EUROPE_GUERNSEY("Europe/Guernsey"),
    EUROPE_HELSINKI("Europe/Helsinki"),
    EUROPE_ISLE_OF_MAN("Europe/Isle_of_Man"),
    EUROPE_ISTANBUL("Europe/Istanbul"),
    EUROPE_JERSEY("Europe/Jersey"),
    EUROPE_KALININGRAD("Europe/Kaliningrad"),
    EUROPE_KIEV("Europe/Kiev"),
    EUROPE_KIROV("Europe/Kirov"),
    EUROPE_KYIV("Europe/Kyiv"),
    EUROPE_LISBON("Europe/Lisbon"),
    EUROPE_LJUBLJANA("Europe/Ljubljana"),
    EUROPE_LONDON("Europe/London"),
    EUROPE_LUXEMBOURG("Europe/Luxembourg"),
    EUROPE_MADRID("Europe/Madrid"),
    EUROPE_MALTA("Europe/Malta"),
    EUROPE_MARIEHAMN("Europe/Mariehamn"),
    EUROPE_MINSK("Europe/Minsk"),
    EUROPE_MONACO("Europe/Monaco"),
    EUROPE_MOSCOW("Europe/Moscow"),
    EUROPE_NICOSIA("Europe/Nicosia"),
    EUROPE_OSLO("Europe/Oslo"),
    EUROPE_PARIS("Europe/Paris"),
    EUROPE_PODGORICA("Europe/Podgorica"),
    EUROPE_PRAGUE("Europe/Prague"),
    EUROPE_RIGA("Europe/Riga"),
    EUROPE_ROME("Europe/Rome"),
    EUROPE_SAMARA("Europe/Samara"),
    EUROPE_SAN_MARINO("Europe/San_Marino"),
    EUROPE_SARAJEVO("Europe/Sarajevo"),
    EUROPE_SARATOV("Europe/Saratov"),
    EUROPE_SIMFEROPOL("Europe/Simferopol"),
    EUROPE_SKOPJE("Europe/Skopje"),
    EUROPE_SOFIA("Europe/Sofia"),
    EUROPE_STOCKHOLM("Europe/Stockholm"),
    EUROPE_TALLINN("Europe/Tallinn"),
    EUROPE_TIRANE("Europe/Tirane"),
    EUROPE_TIRASPOL("Europe/Tiraspol"),
    EUROPE_ULYANOVSK("Europe/Ulyanovsk"),
    EUROPE_UZHGOROD("Europe/Uzhgorod"),
    EUROPE_VADUZ("Europe/Vaduz"),
    EUROPE_VATICAN("Europe/Vatican"),
    EUROPE_VIENNA("Europe/Vienna"),
    EUROPE_VILNIUS("Europe/Vilnius"),
    EUROPE_VOLGOGRAD("Europe/Volgograd"),
    EUROPE_WARSAW("Europe/Warsaw"),
    EUROPE_ZAGREB("Europe/Zagreb"),
    EUROPE_ZAPOROZHYE("Europe/Zaporozhye"),
    EUROPE_ZURICH("Europe/Zurich"),
    GB("GB"),
    GB_EIRE("GB-Eire"),
    GMT("GMT"),
    GMT0("GMT0"),
    GREENWICH("Greenwich"),
    HONGKONG("Hongkong"),
    ICELAND("Iceland"),
    INDIAN_ANTANANARIVO("Indian/Antananarivo"),
    INDIAN_CHAGOS("Indian/Chagos"),
    INDIAN_CHRISTMAS("Indian/Christmas"),
    INDIAN_COCOS("Indian/Cocos"),
    INDIAN_COMORO("Indian/Comoro"),
    INDIAN_KERGUELEN("Indian/Kerguelen"),
    INDIAN_MAHE("Indian/Mahe"),
    INDIAN_MALDIVES("Indian/Maldives"),
    INDIAN_MAURITIUS("Indian/Mauritius"),
    INDIAN_MAYOTTE("Indian/Mayotte"),
    INDIAN_REUNION("Indian/Reunion"),
    IRAN("Iran"),
    ISRAEL("Israel"),
    JAMAICA("Jamaica"),
    JAPAN("Japan"),
    KWAJALEIN("Kwajalein"),
    LIBYA("Libya"),
    MET("MET"),
    MEXICO_BAJANORTE("Mexico/BajaNorte"),
    MEXICO_BAJASUR("Mexico/BajaSur"),
    MEXICO_GENERAL("Mexico/General"),
    MST7MDT("MST7MDT"),
    NAVAJO("Navajo"),
    NZ("NZ"),
    NZ_CHAT("NZ-CHAT"),
    PACIFIC_APIA("Pacific/Apia"),
    PACIFIC_AUCKLAND("Pacific/Auckland"),
    PACIFIC_BOUGAINVILLE("Pacific/Bougainville"),
    PACIFIC_CHATHAM("Pacific/Chatham"),
    PACIFIC_CHUUK("Pacific/Chuuk"),
    PACIFIC_EASTER("Pacific/Easter"),
    PACIFIC_EFATE("Pacific/Efate"),
    PACIFIC_ENDERBURY("Pacific/Enderbury"),
    PACIFIC_FAKAOFO("Pacific/Fakaofo"),
    PACIFIC_FIJI("Pacific/Fiji"),
    PACIFIC_FUNAFUTI("Pacific/Funafuti"),
    PACIFIC_GALAPAGOS("Pacific/Galapagos"),
    PACIFIC_GAMBIER("Pacific/Gambier"),
    PACIFIC_GUADALCANAL("Pacific/Guadalcanal"),
    PACIFIC_GUAM("Pacific/Guam"),
    PACIFIC_HONOLULU("Pacific/Honolulu"),
    PACIFIC_JOHNSTON("Pacific/Johnston"),
    PACIFIC_KANTON("Pacific/Kanton"),
    PACIFIC_KIRITIMATI("Pacific/Kiritimati"),
    PACIFIC_KOSRAE("Pacific/Kosrae"),
    PACIFIC_KWAJALEIN("Pacific/Kwajalein"),
    PACIFIC_MAJURO("Pacific/Majuro"),
    PACIFIC_MARQUESAS("Pacific/Marquesas"),
    PACIFIC_MIDWAY("Pacific/Midway"),
    PACIFIC_NAURU("Pacific/Nauru"),
    PACIFIC_NIUE("Pacific/Niue"),
    PACIFIC_NORFOLK("Pacific/Norfolk"),
    PACIFIC_NOUMEA("Pacific/Noumea"),
    PACIFIC_PAGO_PAGO("Pacific/Pago_Pago"),
    PACIFIC_PALAU("Pacific/Palau"),
    PACIFIC_PITCAIRN("Pacific/Pitcairn"),
    PACIFIC_POHNPEI("Pacific/Pohnpei"),
    PACIFIC_PONAPE("Pacific/Ponape"),
    PACIFIC_PORT_MORESBY("Pacific/Port_Moresby"),
    PACIFIC_RAROTONGA("Pacific/Rarotonga"),
    PACIFIC_SAIPAN("Pacific/Saipan"),
    PACIFIC_SAMOA("Pacific/Samoa"),
    PACIFIC_TAHITI("Pacific/Tahiti"),
    PACIFIC_TARAWA("Pacific/Tarawa"),
    PACIFIC_TONGATAPU("Pacific/Tongatapu"),
    PACIFIC_TRUK("Pacific/Truk"),
    PACIFIC_WAKE("Pacific/Wake"),
    PACIFIC_WALLIS("Pacific/Wallis"),
    PACIFIC_YAP("Pacific/Yap"),
    POLAND("Poland"),
    PORTUGAL("Portugal"),
    PRC("PRC"),
    PST8PDT("PST8PDT"),
    ROK("ROK"),
    SINGAPORE("Singapore"),
    SYSTEMV_AST4("SystemV/AST4"),
    SYSTEMV_AST4ADT("SystemV/AST4ADT"),
    SYSTEMV_CST6("SystemV/CST6"),
    SYSTEMV_CST6CDT("SystemV/CST6CDT"),
    SYSTEMV_EST5("SystemV/EST5"),
    SYSTEMV_EST5EDT("SystemV/EST5EDT"),
    SYSTEMV_HST10("SystemV/HST10"),
    SYSTEMV_MST7("SystemV/MST7"),
    SYSTEMV_MST7MDT("SystemV/MST7MDT"),
    SYSTEMV_PST8("SystemV/PST8"),
    SYSTEMV_PST8PDT("SystemV/PST8PDT"),
    SYSTEMV_YST9("SystemV/YST9"),
    SYSTEMV_YST9YDT("SystemV/YST9YDT"),
    TURKEY("Turkey"),
    UCT("UCT"),
    UNIVERSAL("Universal"),
    US_ALASKA("US/Alaska"),
    US_ALEUTIAN("US/Aleutian"),
    US_ARIZONA("US/Arizona"),
    US_CENTRAL("US/Central"),
    US_EASTERN("US/Eastern"),
    US_EAST_INDIANA("US/East-Indiana"),
    US_HAWAII("US/Hawaii"),
    US_INDIANA_STARKE("US/Indiana-Starke"),
    US_MICHIGAN("US/Michigan"),
    US_MOUNTAIN("US/Mountain"),
    US_PACIFIC("US/Pacific"),
    US_SAMOA("US/Samoa"),
    UTC("UTC"),
    WET("WET"),
    W_SU("W-SU"),
    ZULU("Zulu");

    private final String id;

    TZ(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ZoneId toZoneId() {
        return ZoneId.of(id);
    }

    public TimeZone toJavaUtilTimeZone() {
        return TimeZone.getTimeZone(id);
    }
}
