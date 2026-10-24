/**
 * ClarityVoice Web Client
 * 3D Proximity Spatial Audio & Voice Groups via WebSocket + Web Audio API (HRTF)
 * Multi-Language (9 Languages) & Hardware Audio Device Selection
 */

(function () {
    'use strict';

    // --- Multi-Language Translations (9 Languages) ---
    const I18N = {
        id: {
            announcement_title: '3D PROXIMITY VOICE CHAT ONLINE',
            announcement_desc: 'Dengar dan bicara dengan pemain di sekitarmu secara real-time',
            nav_voice: 'Voice Chat',
            nav_features: 'Fitur & 3D Audio',
            nav_guide: 'Petunjuk OTP',
            status_offline: 'VOICE OFFLINE',
            status_not_connected: 'Belum terhubung',
            status_connecting: 'MENGHUBUNGKAN...',
            status_requesting_mic: 'MEMINTA IZIN...',
            status_waiting_perm: 'Menunggu izin mikrofon',
            status_online: 'VOICE ONLINE',
            status_connected_48k: 'Terhubung 48kHz',
            status_listen_48k: 'Mode Dengar 48kHz',
            status_failed: 'VOICE GAGAL',
            btn_disconnect: 'Putus',
            hero_eyebrow: 'VOICE CHAT LINTAS PLATFORM',
            hero_title_1: 'Lebih dekat dengan',
            hero_title_2: 'suara',
            hero_desc: 'Bergabung ke proximity voice chat server langsung melalui browser tanpa perlu instal mod. Dengar temanmu secara 3D berdasarkan jarak & arah karakter in-game.',
            notice_mic_title: 'Akses Mikrofon Memerlukan HTTPS / Izin Browser',
            notice_mic_desc: 'Browser (Chrome / Edge) membatasi mikrofon pada IP HTTP biasa. Anda tetap bisa terhubung untuk mendengar suara 3D pemain lain (Listen-Only).',
            notice_guide_title: 'Untuk mengaktifkan mikrofon (Solusi Chrome/Edge):',
            otp_label: 'Masukkan 6-Digit Kode OTP dari Minecraft',
            otp_error: 'Kode OTP tidak valid atau telah kedaluwarsa.',
            btn_connect: 'Hubungkan Mikrofon',
            hero_hint_1: 'Gunakan',
            hero_hint_2: 'di dalam game',
            hero_audio_tag: '3D HRTF Web Audio',
            art_spatial: 'SPATIAL AUDIO ENGINE',
            art_opus: '48kHz Opus & HRTF Active',
            hero_no_mod: 'NO MOD REQUIRED FOR WEB',
            hero_radius: 'Proximity Radius: 48 Blocks',
            hero_tech: 'JAVA 25 & WEB AUDIO API',
            dash_eyebrow: 'SESI VOICE TERHUBUNG',
            dash_title_1: 'Ruang Suara',
            dash_title_2: 'Aktif.',
            dash_live_mic: 'LIVE MIKROFON',
            notice_listen_only: 'Mode Hanya Dengar Aktif (Mikrofon Dibatasi Browser)',
            notice_listen_desc: 'Anda dapat mendengar semua pemain di sekitar secara 3D, namun mikrofon dibatasi oleh browser karena menggunakan alamat HTTP biasa.',
            card_audio_controls: 'KONTROL SUARA',
            badge_vad_active: 'VAD Aktif',
            badge_talking: 'Bicara',
            badge_ptt_idle: 'PTT Siaga',
            badge_muted: 'Muted',
            badge_deafened: 'Deafened',
            badge_listen_only: 'Hanya Dengar',
            group_badge: 'GRUP SUARA MOD',
            group_active: 'Terkoneksi ke Voice Group',
            mic_input: 'Input Mikrofon',
            btn_mute: 'Mute Mic',
            btn_unmute: 'Unmute Mic',
            btn_deafen: 'Deafen',
            btn_undeafen: 'Undeafen',
            mode_label: 'Mode Transmisi',
            mode_vad: 'Voice Activity (VAD)',
            mode_ptt: 'Push-to-Talk (PTT)',
            vad_sensitivity: 'Sensitivitas VAD',
            ptt_hold: 'TAHAN UNTUK BICARA',
            ptt_keys: 'Tekan tombol [Space] atau [V]',
            device_input: 'Perangkat Mikrofon (Input)',
            device_output: 'Perangkat Audio (Output/Speaker)',
            coords_title: 'Posisi Karakter In-Game',
            coords_spatial: 'HRTF Spatial Audio',
            coords_radius: 'Radius',
            coords_blocks: 'Blok',
            card_radar: '3D SPATIAL RADAR',
            nearby_title: 'Pemain di Sekitar',
            master_vol: 'Master:',
            nearby_empty_title: 'Tidak ada pemain di dekatmu',
            nearby_empty_desc: 'Pemain dalam radius 48 blok di world yang sama akan otomatis terdengar dan muncul di sini.',
            feat_1_title: 'Langsung Masuk',
            feat_1_desc: 'Cukup buka browser & masukkan OTP',
            feat_2_title: '3D Spatial HRTF',
            feat_2_desc: 'Arah & jarak suara presisi sesuai game',
            feat_3_title: 'Lintas Platform',
            feat_3_desc: 'Mod Simple Voice Chat ↔ Web Client',
            feat_4_title: 'Kontrol Independen',
            feat_4_desc: 'Atur volume tiap pemain tersendiri',
            guide_title: 'Cara menggunakan Clarity Web Voice?',
            guide_desc: 'Ketik /voice otp di chat Minecraft untuk mendapatkan 6 digit kode instan.',
            guide_start: 'Mulai Bicara',
            footer_copy: '© 2026 Clarity Realm. Web Voice Chat Powered by Simple Voice Chat API.',
            toast_perm_prompt: 'Silakan klik "Izinkan" (Allow) pada pop-up izin browser untuk mikrofon.',
            toast_listen_only: 'Mode Dengar Aktif: Mikrofon dibatasi oleh browser.',
            toast_welcome: 'Selamat datang, {name}! Voice aktif.',
            toast_session_dc: 'Sesi Voice Chat diputuskan.',
            toast_mic_muted: 'Mikrofon dimatikan (Muted)',
            toast_mic_unmuted: 'Mikrofon aktif (Unmuted)',
            toast_deafened: 'Audio disenyapkan (Deafened)',
            toast_undeafened: 'Audio dinyalakan kembali'
        },
        en: {
            announcement_title: '3D PROXIMITY VOICE CHAT ONLINE',
            announcement_desc: 'Hear and talk with nearby players in real-time',
            nav_voice: 'Voice Chat',
            nav_features: 'Features & 3D Audio',
            nav_guide: 'OTP Guide',
            status_offline: 'VOICE OFFLINE',
            status_not_connected: 'Not connected',
            status_connecting: 'CONNECTING...',
            status_requesting_mic: 'REQUESTING PERMISSION...',
            status_waiting_perm: 'Waiting for mic permission',
            status_online: 'VOICE ONLINE',
            status_connected_48k: 'Connected 48kHz',
            status_listen_48k: 'Listen-Only 48kHz',
            status_failed: 'VOICE FAILED',
            btn_disconnect: 'Disconnect',
            hero_eyebrow: 'CROSS-PLATFORM VOICE CHAT',
            hero_title_1: 'Get closer with',
            hero_title_2: '3D spatial',
            hero_desc: 'Join the server proximity voice chat directly from your browser without installing mods. Hear friends in true 3D based on in-game character position and direction.',
            notice_mic_title: 'Microphone Access Requires HTTPS / Browser Permission',
            notice_mic_desc: 'Browsers restrict microphone access on plain HTTP. You can still connect to hear other players in 3D (Listen-Only).',
            notice_guide_title: 'To enable microphone in Chrome/Edge:',
            otp_label: 'Enter 6-Digit OTP Code from Minecraft',
            otp_error: 'Invalid or expired OTP code.',
            btn_connect: 'Connect Microphone',
            hero_hint_1: 'Use',
            hero_hint_2: 'in-game',
            hero_audio_tag: '3D HRTF Web Audio',
            art_spatial: 'SPATIAL AUDIO ENGINE',
            art_opus: '48kHz Opus & HRTF Active',
            hero_no_mod: 'NO MOD REQUIRED FOR WEB',
            hero_radius: 'Proximity Radius: 48 Blocks',
            hero_tech: 'JAVA 25 & WEB AUDIO API',
            dash_eyebrow: 'VOICE SESSION CONNECTED',
            dash_title_1: 'Voice Room',
            dash_title_2: 'Active.',
            dash_live_mic: 'LIVE MICROPHONE',
            notice_listen_only: 'Listen-Only Mode Active (Mic Restricted by Browser)',
            notice_listen_desc: 'You can hear everyone nearby in 3D, but your microphone is restricted by the browser due to plain HTTP.',
            card_audio_controls: 'AUDIO CONTROLS',
            badge_vad_active: 'VAD Active',
            badge_talking: 'Speaking',
            badge_ptt_idle: 'PTT Idle',
            badge_muted: 'Muted',
            badge_deafened: 'Deafened',
            badge_listen_only: 'Listen-Only',
            group_badge: 'MOD VOICE GROUP',
            group_active: 'Connected to Voice Group',
            mic_input: 'Microphone Input',
            btn_mute: 'Mute Mic',
            btn_unmute: 'Unmute Mic',
            btn_deafen: 'Deafen',
            btn_undeafen: 'Undeafen',
            mode_label: 'Transmission Mode',
            mode_vad: 'Voice Activity (VAD)',
            mode_ptt: 'Push-to-Talk (PTT)',
            vad_sensitivity: 'VAD Sensitivity',
            ptt_hold: 'HOLD TO TALK',
            ptt_keys: 'Press [Space] or [V] key',
            device_input: 'Microphone Device (Input)',
            device_output: 'Audio Device (Output/Speaker)',
            coords_title: 'In-Game Character Position',
            coords_spatial: 'HRTF Spatial Audio',
            coords_radius: 'Radius',
            coords_blocks: 'Blocks',
            card_radar: '3D SPATIAL RADAR',
            nearby_title: 'Nearby Players',
            master_vol: 'Master:',
            nearby_empty_title: 'No players nearby',
            nearby_empty_desc: 'Players within 48 blocks in the same world will automatically appear and be heard here.',
            feat_1_title: 'Instant Access',
            feat_1_desc: 'Simply open browser & enter OTP',
            feat_2_title: '3D Spatial HRTF',
            feat_2_desc: 'Precise distance and direction audio',
            feat_3_title: 'Cross-Platform',
            feat_3_desc: 'Simple Voice Chat Mod ↔ Web Client',
            feat_4_title: 'Independent Controls',
            feat_4_desc: 'Adjust each player volume individually',
            guide_title: 'How to use Clarity Web Voice?',
            guide_desc: 'Type /voice otp in Minecraft chat to receive your 6-digit code.',
            guide_start: 'Start Talking',
            footer_copy: '© 2026 Clarity Realm. Web Voice Chat Powered by Simple Voice Chat API.',
            toast_perm_prompt: 'Please click "Allow" on the browser microphone permission popup.',
            toast_listen_only: 'Listen-Only Mode: Microphone restricted by browser.',
            toast_welcome: 'Welcome, {name}! Voice connected.',
            toast_session_dc: 'Voice Chat session disconnected.',
            toast_mic_muted: 'Microphone muted',
            toast_mic_unmuted: 'Microphone unmuted',
            toast_deafened: 'Audio deafened',
            toast_undeafened: 'Audio undeafened'
        },
        de: {
            announcement_title: '3D NÄHERUNGS-VOICE-CHAT ONLINE',
            announcement_desc: 'Höre und sprich mit Spielern in deiner Nähe in Echtzeit',
            nav_voice: 'Voice-Chat',
            nav_features: 'Funktionen & 3D-Audio',
            nav_guide: 'OTP-Anleitung',
            status_offline: 'VOICE OFFLINE',
            status_not_connected: 'Nicht verbunden',
            status_online: 'VOICE ONLINE',
            btn_disconnect: 'Trennen',
            hero_title_1: 'Näher dran mit',
            hero_title_2: '3D räumlichem',
            btn_connect: 'Mikrofon verbinden',
            group_badge: 'MOD SPRACHGRUPPE',
            group_active: 'Verbunden mit Sprachgruppe',
            btn_mute: 'Stummschalten',
            btn_unmute: 'Entstummen',
            btn_deafen: 'Taub schalten',
            btn_undeafen: 'Enttauben',
            device_input: 'Mikrofon (Eingang)',
            device_output: 'Audioausgabe (Lautsprecher)',
            nearby_title: 'Spieler in der Nähe'
        },
        es: {
            announcement_title: 'VOZ POR PROXIMIDAD 3D EN LÍNEA',
            announcement_desc: 'Escucha y habla con jugadores cercanos en tiempo real',
            nav_voice: 'Chat de Voz',
            nav_features: 'Funciones y Audio 3D',
            nav_guide: 'Guía OTP',
            status_offline: 'VOZ DESCONECTADA',
            status_not_connected: 'No conectado',
            status_online: 'VOZ EN LÍNEA',
            btn_disconnect: 'Desconectar',
            hero_title_1: 'Más cerca con',
            hero_title_2: 'sonido',
            btn_connect: 'Conectar Micrófono',
            group_badge: 'GRUPO DE VOZ MOD',
            group_active: 'Conectado al Grupo de Voz',
            btn_mute: 'Silenciar Mic',
            btn_unmute: 'Activar Mic',
            btn_deafen: 'Ensordecer',
            btn_undeafen: 'Desensordecer',
            device_input: 'Micrófono (Entrada)',
            device_output: 'Dispositivo de Audio (Salida)',
            nearby_title: 'Jugadores Cercanos'
        },
        fr: {
            announcement_title: 'VOICE CHAT DE PROXIMITÉ 3D EN LIGNE',
            announcement_desc: 'Écoutez et parlez avec les joueurs proches en temps réel',
            nav_voice: 'Chat Vocal',
            nav_features: 'Fonctionnalités & Audio 3D',
            nav_guide: 'Guide OTP',
            status_offline: 'VOIX DÉCONNECTÉE',
            status_not_connected: 'Non connecté',
            status_online: 'VOIX EN LIGNE',
            btn_disconnect: 'Déconnecter',
            hero_title_1: 'Plus proche avec le',
            hero_title_2: 'son',
            btn_connect: 'Connecter le Micro',
            group_badge: 'GROUPE VOCAL MOD',
            group_active: 'Connecté au Groupe Vocal',
            btn_mute: 'Muet',
            btn_unmute: 'Activer Micro',
            btn_deafen: 'Assourdir',
            btn_undeafen: 'Désassourdir',
            device_input: 'Microphone (Entrée)',
            device_output: 'Périphérique Audio (Sortie)',
            nearby_title: 'Joueurs à Proximité'
        },
        ja: {
            announcement_title: '3D近接ボイスチャット オンライン',
            announcement_desc: '近くのプレイヤーとリアルタイムで通話可能',
            nav_voice: 'ボイスチャット',
            nav_features: '機能と3D音響',
            nav_guide: 'OTPガイド',
            status_offline: 'ボイス オフライン',
            status_not_connected: '未接続',
            status_online: 'ボイス オンライン',
            btn_disconnect: '切断',
            hero_title_1: '3D空間オーディオで',
            hero_title_2: 'もっと',
            btn_connect: 'マイクを接続',
            group_badge: 'MOD音声グループ',
            group_active: '音声グループに接続中',
            btn_mute: 'ミュート',
            btn_unmute: 'ミュート解除',
            btn_deafen: 'スピーカーオフ',
            btn_undeafen: 'スピーカーオン',
            device_input: 'マイク入力デバイス',
            device_output: '音声出力デバイス (スピーカー)',
            nearby_title: '近くのプレイヤー'
        },
        pt: {
            announcement_title: 'VOICE CHAT DE PROXIMIDADE 3D ONLINE',
            announcement_desc: 'Ouça e fale com jogadores próximos em tempo real',
            nav_voice: 'Chat de Voz',
            nav_features: 'Recursos e Áudio 3D',
            nav_guide: 'Guia OTP',
            status_offline: 'VOZ DESCONECTADA',
            status_not_connected: 'Não conectado',
            status_online: 'VOZ ONLINE',
            btn_disconnect: 'Desconectar',
            hero_title_1: 'Mais perto com',
            hero_title_2: 'som',
            btn_connect: 'Conectar Microfone',
            group_badge: 'GRUPO DE VOZ MOD',
            group_active: 'Conectado ao Grupo de Voz',
            btn_mute: 'Mutar Mic',
            btn_unmute: 'Desmutar Mic',
            btn_deafen: 'Ensurdecer',
            btn_undeafen: 'Desensurdecer',
            device_input: 'Microfone (Entrada)',
            device_output: 'Dispositivo de Áudio (Saída)',
            nearby_title: 'Jogadores Próximos'
        },
        ru: {
            announcement_title: '3D ПРОКСИМИТИ ГОЛОСОВОЙ ЧАТ ОНЛАЙН',
            announcement_desc: 'Слышьте и говорите с игроками поблизости в реальном времени',
            nav_voice: 'Голосовой чат',
            nav_features: 'Возможности и 3D-звук',
            nav_guide: 'Инструкция OTP',
            status_offline: 'ГОЛОС ОТКЛЮЧЕН',
            status_not_connected: 'Не подключено',
            status_online: 'ГОЛОС ОНЛАЙН',
            btn_disconnect: 'Отключиться',
            hero_title_1: 'Станьте ближе с',
            hero_title_2: '3D',
            btn_connect: 'Подключить микрофон',
            group_badge: 'МОД ГОЛОСОВАЯ ГРУППА',
            group_active: 'Подключено к голосовой группе',
            btn_mute: 'Заглушить микрофон',
            btn_unmute: 'Включить микрофон',
            btn_deafen: 'Заглушить звук',
            btn_undeafen: 'Включить звук',
            device_input: 'Микрофон (Вход)',
            device_output: 'Аудиоустройство (Выход)',
            nearby_title: 'Игроки поблизости'
        },
        zh: {
            announcement_title: '3D 空间距离语音已上线',
            announcement_desc: '与身边的玩家进行超低延迟近距离实时语音',
            nav_voice: '语音聊天',
            nav_features: '特性与3D音频',
            nav_guide: 'OTP验证指南',
            status_offline: '语音离线',
            status_not_connected: '未连接',
            status_online: '语音在线',
            btn_disconnect: '断开连接',
            hero_title_1: '更身临其境的',
            hero_title_2: '3D空间',
            btn_connect: '连接麦克风',
            group_badge: 'MOD语音群组',
            group_active: '已连接至语音群组',
            btn_mute: '静音麦克风',
            btn_unmute: '取消静音',
            btn_deafen: '闭麦静音',
            btn_undeafen: '取消闭麦',
            device_input: '麦克风设备 (输入)',
            device_output: '音频设备 (输出/扬声器)',
            nearby_title: '附近的玩家'
        }
    };

    // --- State Variables ---
    let currentLang = 'id';
    let socket = null;
    let audioContext = null;
    let micStream = null;
    let micWorkletNode = null;
    let micGainNode = null;
    let micAnalyser = null;
    let vuAnimId = null;

    let isMuted = false;
    let isDeafened = false;
    let isPttMode = false;
    let isPttPressed = false;
    let isMicAvailable = false;
    let isWorkletModuleLoaded = false;
    let vadThreshold = 25; // 0 - 100%
    let masterVolume = 1.0;

    let myUuid = null;
    let myName = null;
    let maxAudibleDistance = 48.0;

    let selectedMicId = '';
    let selectedSpeakerId = '';

    // Speakers Maps
    const speakers = new Map(); // uuid -> { panner, gain, nextStartTime, timeoutId }
    const groupSpeakers = new Map(); // uuid -> { gain, nextStartTime, timeoutId }
    const speakerVolumes = new Map(); // uuid -> volume float (0.0 to 2.0)
    let nearbyPlayersCache = [];

    let pingInterval = null;
    let lastPingTimestamp = 0;
    let toastTimer = null;

    // --- DOM Elements ---
    const loginSection = document.getElementById('loginSection');
    const dashboardSection = document.getElementById('dashboardSection');
    const langSelect = document.getElementById('langSelect');

    const voiceStatusBadge = document.getElementById('voiceStatusBadge');
    const statusDot = document.getElementById('statusDot');
    const statusTitle = document.getElementById('statusTitle');
    const statusSubtitle = document.getElementById('statusSubtitle');

    const pingBadge = document.getElementById('pingBadge');
    const pingValue = document.getElementById('pingValue');
    const userProfileChip = document.getElementById('userProfileChip');
    const userAvatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');
    const btnDisconnectTop = document.getElementById('btnDisconnectTop');

    const otpDigits = [
        document.getElementById('otp1'),
        document.getElementById('otp2'),
        document.getElementById('otp3'),
        document.getElementById('otp4'),
        document.getElementById('otp5'),
        document.getElementById('otp6')
    ];
    const otpError = document.getElementById('otpError');
    const btnConnect = document.getElementById('btnConnect');

    const vuBar = document.getElementById('vuBar');
    const vuValueText = document.getElementById('vuValueText');
    const vadThresholdMarker = document.getElementById('vadThresholdMarker');
    const micStateBadge = document.getElementById('micStateBadge');

    const voiceGroupCard = document.getElementById('voiceGroupCard');
    const groupNameText = document.getElementById('groupNameText');
    const groupTypeText = document.getElementById('groupTypeText');

    const btnMute = document.getElementById('btnMute');
    const btnMuteText = document.getElementById('btnMuteText');
    const btnDeafen = document.getElementById('btnDeafen');
    const btnDeafenText = document.getElementById('btnDeafenText');

    const modeVad = document.getElementById('modeVad');
    const modePtt = document.getElementById('modePtt');
    const vadSettingsGroup = document.getElementById('vadSettingsGroup');
    const vadSlider = document.getElementById('vadSlider');
    const vadVal = document.getElementById('vadVal');
    const pttGroup = document.getElementById('pttGroup');
    const btnPtt = document.getElementById('btnPtt');

    const micDeviceSelect = document.getElementById('micDeviceSelect');
    const speakerDeviceSelect = document.getElementById('speakerDeviceSelect');
    const speakerSettingRow = document.getElementById('speakerSettingRow');

    const coordX = document.getElementById('coordX');
    const coordY = document.getElementById('coordY');
    const coordZ = document.getElementById('coordZ');
    const worldBadge = document.getElementById('worldBadge');
    const maxDistText = document.getElementById('maxDistText');

    const nearbyCount = document.getElementById('nearbyCount');
    const nearbyPlayersList = document.getElementById('nearbyPlayersList');
    const masterVolumeSlider = document.getElementById('masterVolume');
    const toast = document.getElementById('toast');

    // --- Inline AudioWorklet Processor ---
    const audioWorkletBlobCode = `
    class ClarityAudioProcessor extends AudioWorkletProcessor {
        constructor() {
            super();
            this.buffer = new Float32Array(960);
            this.bufferIndex = 0;
        }

        process(inputs, outputs, parameters) {
            const input = inputs[0];
            if (!input || !input[0]) return true;

            const channelData = input[0];
            for (let i = 0; i < channelData.length; i++) {
                this.buffer[this.bufferIndex++] = channelData[i];
                if (this.bufferIndex >= 960) {
                    this.port.postMessage(this.buffer.slice(0, 960));
                    this.bufferIndex = 0;
                }
            }
            return true;
        }
    }
    registerProcessor('clarity-audio-processor', ClarityAudioProcessor);
    `;

    // --- Lifecycle Initialization ---
    document.addEventListener('DOMContentLoaded', () => {
        initLanguage();
        checkInsecureContext();
        setupOtpInputs();
        setupControls();
        checkUrlForOtp();
        initDeviceEnumeration();
    });

    function initLanguage() {
        const savedLang = localStorage.getItem('clarity_voice_lang');
        const browserLang = (navigator.language || 'id').slice(0, 2).toLowerCase();
        currentLang = savedLang || (I18N[browserLang] ? browserLang : 'id');

        if (langSelect) {
            langSelect.value = currentLang;
            langSelect.addEventListener('change', (e) => {
                setLanguage(e.target.value);
            });
        }
        applyLanguage(currentLang);
    }

    function setLanguage(lang) {
        if (!I18N[lang]) lang = 'en';
        currentLang = lang;
        localStorage.setItem('clarity_voice_lang', lang);
        applyLanguage(lang);
    }

    function t(key, params = {}) {
        const dict = I18N[currentLang] || I18N.id;
        let str = dict[key] || I18N.en[key] || I18N.id[key] || key;
        for (const [k, v] of Object.entries(params)) {
            str = str.replace(`{${k}}`, v);
        }
        return str;
    }

    function applyLanguage(lang) {
        document.documentElement.lang = lang;
        const elements = document.querySelectorAll('[data-i18n]');
        elements.forEach(el => {
            const key = el.getAttribute('data-i18n');
            const translation = t(key);
            if (translation) {
                el.textContent = translation;
            }
        });
        updateMicBadge();
    }

    function checkInsecureContext() {
        const isSecure = window.isSecureContext ||
            window.location.hostname === 'localhost' ||
            window.location.hostname === '127.0.0.1';

        const originDisplays = document.querySelectorAll('#originDisplay, #originCopyText, .originDisplayDynamic');
        originDisplays.forEach(el => {
            el.textContent = window.location.origin;
        });

        const copyTrigger = document.getElementById('originCopyText');
        if (copyTrigger) {
            copyTrigger.style.cursor = 'pointer';
            copyTrigger.title = 'Salin alamat';
            copyTrigger.addEventListener('click', () => {
                if (navigator.clipboard) {
                    navigator.clipboard.writeText(window.location.origin);
                    showToast('Alamat disalin: ' + window.location.origin);
                }
            });
        }

        if (!isSecure && (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia)) {
            const notice = document.getElementById('insecureHttpNotice');
            if (notice) {
                notice.classList.remove('hidden');
            }
        }
    }

    function showToast(message) {
        if (!toast) return;
        toast.textContent = message;
        toast.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.classList.remove('show');
        }, 2800);
    }

    async function checkUrlForOtp() {
        const urlParams = new URLSearchParams(window.location.search);
        const otpParam = urlParams.get('otp');
        if (otpParam && otpParam.length === 6) {
            for (let i = 0; i < 6; i++) {
                otpDigits[i].value = otpParam[i];
            }
            btnConnect.scrollIntoView({ behavior: 'smooth' });
            btnConnect.classList.add('pulse-ready');
            btnConnect.focus();
        }
    }

    // --- OTP Input Management ---
    function setupOtpInputs() {
        otpDigits.forEach((digitInput, idx) => {
            digitInput.addEventListener('input', (e) => {
                const val = e.target.value.replace(/[^0-9]/g, '');
                e.target.value = val;

                if (val && idx < 5) {
                    otpDigits[idx + 1].focus();
                    otpDigits[idx + 1].select();
                }

                if (getEnteredOtp().length === 6) {
                    otpError.classList.add('hidden');
                    btnConnect.classList.add('pulse-ready');
                }
            });

            digitInput.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && !digitInput.value && idx > 0) {
                    otpDigits[idx - 1].focus();
                } else if (e.key === 'Enter') {
                    startConnection();
                }
            });

            digitInput.addEventListener('paste', (e) => {
                e.preventDefault();
                const pasteData = (e.clipboardData || window.clipboardData).getData('text').trim();
                const cleanDigits = pasteData.replace(/[^0-9]/g, '').slice(0, 6);
                for (let i = 0; i < cleanDigits.length; i++) {
                    otpDigits[i].value = cleanDigits[i];
                }
                if (cleanDigits.length < 6) {
                    otpDigits[cleanDigits.length].focus();
                } else {
                    otpDigits[5].focus();
                    btnConnect.classList.add('pulse-ready');
                }
            });
        });
    }

    function getEnteredOtp() {
        return otpDigits.map(d => d.value.trim()).join('');
    }

    // --- Audio Hardware Device Management ---
    async function initDeviceEnumeration() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) {
            return;
        }

        try {
            await refreshDeviceLists();
            navigator.mediaDevices.addEventListener('devicechange', refreshDeviceLists);
        } catch (ignored) {}

        if (micDeviceSelect) {
            micDeviceSelect.addEventListener('change', async (e) => {
                selectedMicId = e.target.value;
                localStorage.setItem('clarity_voice_mic_id', selectedMicId);
                if (socket && socket.readyState === WebSocket.OPEN) {
                    showToast('Mengganti mikrofon...');
                    await initAudio(selectedMicId);
                }
            });
        }

        if (speakerDeviceSelect) {
            speakerDeviceSelect.addEventListener('change', async (e) => {
                selectedSpeakerId = e.target.value;
                localStorage.setItem('clarity_voice_speaker_id', selectedSpeakerId);
                applySpeakerDevice(selectedSpeakerId);
            });
        }
    }

    async function refreshDeviceLists() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) return;

        try {
            const devices = await navigator.mediaDevices.enumerateDevices();
            const mics = devices.filter(d => d.kind === 'audioinput');
            const speakersList = devices.filter(d => d.kind === 'audiooutput');

            selectedMicId = localStorage.getItem('clarity_voice_mic_id') || '';
            selectedSpeakerId = localStorage.getItem('clarity_voice_speaker_id') || '';

            if (micDeviceSelect) {
                micDeviceSelect.innerHTML = '';
                if (mics.length === 0) {
                    const opt = document.createElement('option');
                    opt.value = '';
                    opt.textContent = 'Default Microphone';
                    micDeviceSelect.appendChild(opt);
                } else {
                    mics.forEach((m, idx) => {
                        const opt = document.createElement('option');
                        opt.value = m.deviceId;
                        opt.textContent = m.label || `Microphone ${idx + 1}`;
                        if (m.deviceId === selectedMicId) opt.selected = true;
                        micDeviceSelect.appendChild(opt);
                    });
                }
            }

            if (speakerDeviceSelect) {
                speakerDeviceSelect.innerHTML = '';
                if (speakersList.length === 0) {
                    if (speakerSettingRow) speakerSettingRow.classList.add('hidden');
                } else {
                    if (speakerSettingRow) speakerSettingRow.classList.remove('hidden');
                    speakersList.forEach((s, idx) => {
                        const opt = document.createElement('option');
                        opt.value = s.deviceId;
                        opt.textContent = s.label || `Speaker ${idx + 1}`;
                        if (s.deviceId === selectedSpeakerId) opt.selected = true;
                        speakerDeviceSelect.appendChild(opt);
                    });
                }
            }
        } catch (ignored) {}
    }

    async function applySpeakerDevice(deviceId) {
        if (!audioContext) return;
        if (typeof audioContext.setSinkId === 'function') {
            try {
                await audioContext.setSinkId(deviceId);
                showToast('Output speaker diperbarui');
            } catch (err) {
                console.warn('Gagal mengatur sink audioContext:', err);
            }
        }
    }

    // --- Control Actions ---
    function setupControls() {
        btnConnect.addEventListener('click', startConnection);
        btnDisconnectTop.addEventListener('click', disconnectVoice);

        // Mute Toggle
        btnMute.addEventListener('click', () => {
            setMute(!isMuted);
        });

        // Deafen Toggle
        btnDeafen.addEventListener('click', () => {
            setDeafen(!isDeafened);
        });

        // Mode Toggles
        modeVad.addEventListener('click', () => {
            isPttMode = false;
            modeVad.classList.add('active');
            modePtt.classList.remove('active');
            vadSettingsGroup.classList.remove('hidden');
            pttGroup.classList.add('hidden');
            updateMicBadge();
            showToast(t('mode_vad'));
        });

        modePtt.addEventListener('click', () => {
            isPttMode = true;
            modePtt.classList.add('active');
            modeVad.classList.remove('active');
            vadSettingsGroup.classList.add('hidden');
            pttGroup.classList.remove('hidden');
            updateMicBadge();
            showToast(t('mode_ptt'));
        });

        // VAD Threshold Slider
        vadSlider.addEventListener('input', (e) => {
            vadThreshold = parseInt(e.target.value, 10);
            vadVal.textContent = vadThreshold + '%';
            vadThresholdMarker.style.left = vadThreshold + '%';
        });

        // Master Volume Slider
        masterVolumeSlider.addEventListener('input', (e) => {
            masterVolume = parseInt(e.target.value, 10) / 100.0;
            updateAllSpeakerGains();
        });

        // Push-to-Talk (Mouse & Touch)
        const startPtt = (e) => {
            e.preventDefault();
            isPttPressed = true;
            btnPtt.classList.add('ptt-active');
            updateMicBadge();
        };

        const stopPtt = (e) => {
            e.preventDefault();
            isPttPressed = false;
            btnPtt.classList.remove('ptt-active');
            updateMicBadge();
        };

        btnPtt.addEventListener('mousedown', startPtt);
        btnPtt.addEventListener('mouseup', stopPtt);
        btnPtt.addEventListener('touchstart', startPtt, { passive: false });
        btnPtt.addEventListener('touchend', stopPtt, { passive: false });

        // PTT Keyboard bindings (Space or V)
        window.addEventListener('keydown', (e) => {
            if (!isPttMode || isPttPressed) return;
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') return;

            if (e.code === 'KeyV' || e.code === 'Space') {
                if (e.code === 'Space') e.preventDefault();
                isPttPressed = true;
                btnPtt.classList.add('ptt-active');
                updateMicBadge();
            }
        });

        window.addEventListener('keyup', (e) => {
            if (!isPttMode || !isPttPressed) return;
            if (e.code === 'KeyV' || e.code === 'Space') {
                isPttPressed = false;
                btnPtt.classList.remove('ptt-active');
                updateMicBadge();
            }
        });
    }

    function setMute(mute) {
        if (!isMicAvailable) {
            showToast(t('toast_listen_only'));
            return;
        }
        isMuted = mute;
        if (isMuted) {
            btnMute.classList.add('active-danger');
            btnMuteText.textContent = t('btn_unmute');
            showToast(t('toast_mic_muted'));
        } else {
            btnMute.classList.remove('active-danger');
            btnMuteText.textContent = t('btn_mute');
            showToast(t('toast_mic_unmuted'));
        }
        updateMicBadge();
        sendJson({ type: 'mute', value: isMuted });
    }

    function setDeafen(deafen) {
        isDeafened = deafen;
        if (isDeafened) {
            btnDeafen.classList.add('active-danger');
            btnDeafenText.textContent = t('btn_undeafen');
            if (!isMuted && isMicAvailable) setMute(true);
            showToast(t('toast_deafened'));
        } else {
            btnDeafen.classList.remove('active-danger');
            btnDeafenText.textContent = t('btn_deafen');
            if (isMuted && isMicAvailable) setMute(false);
            showToast(t('toast_undeafened'));
        }
        updateMicBadge();
        sendJson({ type: 'deafen', value: isDeafened });
    }

    function updateMicBadge() {
        if (!micStateBadge) return;
        if (!isMicAvailable) {
            micStateBadge.textContent = t('badge_listen_only');
            micStateBadge.className = 'card-status-pill pill-danger';
            return;
        }
        if (isMuted || isDeafened) {
            micStateBadge.textContent = isDeafened ? t('badge_deafened') : t('badge_muted');
            micStateBadge.className = 'card-status-pill pill-danger';
        } else if (isPttMode) {
            if (isPttPressed) {
                micStateBadge.textContent = t('badge_talking');
                micStateBadge.className = 'card-status-pill';
            } else {
                micStateBadge.textContent = t('badge_ptt_idle');
                micStateBadge.className = 'card-status-pill';
            }
        } else {
            micStateBadge.textContent = t('badge_vad_active');
            micStateBadge.className = 'card-status-pill';
        }
    }

    // --- Audio Pipeline & Mic Capture ---
    async function initAudio(preferredDeviceId = '') {
        if (!audioContext) {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            audioContext = new AudioCtx({ sampleRate: 48000 });
        }

        if (audioContext.state === 'suspended') {
            await audioContext.resume();
        }

        if (selectedSpeakerId) {
            applySpeakerDevice(selectedSpeakerId);
        }

        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            console.warn('getUserMedia unavailable. Running Listen-Only.');
            isMicAvailable = false;
            updateMicBadge();
            return false;
        }

        try {
            if (micStream) {
                try {
                    micStream.getTracks().forEach(t => t.stop());
                } catch (ignored) {}
                micStream = null;
            }

            const audioConstraints = {
                channelCount: { ideal: 1 },
                echoCancellation: true,
                noiseSuppression: true,
                autoGainControl: true
            };
            if (preferredDeviceId) {
                audioConstraints.deviceId = { exact: preferredDeviceId };
            }

            micStream = await navigator.mediaDevices.getUserMedia({ audio: audioConstraints });
            isMicAvailable = true;

            // Re-enumerate devices now that label access has been granted by user
            refreshDeviceLists();

            const micSource = audioContext.createMediaStreamSource(micStream);
            micGainNode = audioContext.createGain();
            micGainNode.gain.value = 1.0;
            micSource.connect(micGainNode);

            micAnalyser = audioContext.createAnalyser();
            micAnalyser.fftSize = 256;
            micAnalyser.smoothingTimeConstant = 0.2;
            micGainNode.connect(micAnalyser);
            startVuMeterLoop();

            const silentDest = audioContext.createMediaStreamDestination();

            try {
                if (!isWorkletModuleLoaded) {
                    const blob = new Blob([audioWorkletBlobCode], { type: 'application/javascript' });
                    const workletUrl = URL.createObjectURL(blob);
                    await audioContext.audioWorklet.addModule(workletUrl);
                    isWorkletModuleLoaded = true;
                }

                micWorkletNode = new AudioWorkletNode(audioContext, 'clarity-audio-processor');
                micWorkletNode.port.onmessage = (event) => {
                    handleMicPcmChunk(event.data);
                };

                micGainNode.connect(micWorkletNode);
                micWorkletNode.connect(silentDest);
            } catch (workletError) {
                console.warn('AudioWorklet fallback to ScriptProcessor:', workletError);
                setupScriptProcessorFallback(micGainNode, silentDest);
            }

            updateMicBadge();
            return true;
        } catch (micErr) {
            console.warn('Mic access error (denied or unavailable):', micErr);
            isMicAvailable = false;
            updateMicBadge();
            return false;
        }
    }

    function startVuMeterLoop() {
        if (vuAnimId) {
            cancelAnimationFrame(vuAnimId);
            vuAnimId = null;
        }

        const dataArray = new Uint8Array(micAnalyser ? micAnalyser.frequencyBinCount : 128);

        function render() {
            if (!isMicAvailable || isMuted || isDeafened || !micAnalyser) {
                if (vuBar) vuBar.style.width = '0%';
                if (vuValueText) vuValueText.textContent = (isMuted || isDeafened) ? t('badge_muted') : '0%';
                vuAnimId = requestAnimationFrame(render);
                return;
            }

            micAnalyser.getByteTimeDomainData(dataArray);

            let sumSquares = 0;
            for (let i = 0; i < dataArray.length; i++) {
                const norm = (dataArray[i] - 128) / 128.0;
                sumSquares += norm * norm;
            }
            const rms = Math.sqrt(sumSquares / dataArray.length);

            let volumePercent = 0;
            if (rms > 0.001) {
                const db = 20 * Math.log10(rms);
                volumePercent = Math.max(0, Math.min(100, Math.round(((db + 52) / 42) * 100)));
            }

            if (vuBar && vuValueText) {
                vuBar.style.width = volumePercent + '%';
                vuValueText.textContent = volumePercent + '%';
            }

            vuAnimId = requestAnimationFrame(render);
        }

        vuAnimId = requestAnimationFrame(render);
    }

    function setupScriptProcessorFallback(sourceNode, silentDest) {
        const processor = audioContext.createScriptProcessor(2048, 1, 1);
        let tempBuffer = new Float32Array(960);
        let tempIndex = 0;

        processor.onaudioprocess = (e) => {
            const input = e.inputBuffer.getChannelData(0);
            for (let i = 0; i < input.length; i++) {
                tempBuffer[tempIndex++] = input[i];
                if (tempIndex >= 960) {
                    handleMicPcmChunk(tempBuffer.slice(0, 960));
                    tempIndex = 0;
                }
            }
        };

        sourceNode.connect(processor);
        if (silentDest) {
            processor.connect(silentDest);
        }
    }

    function handleMicPcmChunk(float32Chunk) {
        if (!float32Chunk || float32Chunk.length !== 960) return;

        let sumSquares = 0;
        for (let i = 0; i < float32Chunk.length; i++) {
            sumSquares += float32Chunk[i] * float32Chunk[i];
        }
        const rms = Math.sqrt(sumSquares / float32Chunk.length);

        let volumePercent = 0;
        if (rms > 0.001) {
            const db = 20 * Math.log10(rms);
            volumePercent = Math.max(0, Math.min(100, Math.round(((db + 52) / 42) * 100)));
        }

        if (!socket || socket.readyState !== WebSocket.OPEN) return;
        if (isMuted || isDeafened) return;

        let shouldTransmit = false;
        if (isPttMode) {
            shouldTransmit = isPttPressed;
        } else {
            shouldTransmit = volumePercent >= vadThreshold;
        }

        if (!shouldTransmit) return;

        const buffer = new ArrayBuffer(1 + 960 * 2);
        const view = new DataView(buffer);
        view.setUint8(0, 0x02); // PACKET_TYPE_CLIENT_MIC

        let offset = 1;
        for (let i = 0; i < float32Chunk.length; i++, offset += 2) {
            let s = Math.max(-1, Math.min(1, float32Chunk[i]));
            view.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
        }

        socket.send(buffer);
    }

    // --- WebSocket Engine ---
    async function startConnection() {
        const otp = getEnteredOtp();
        if (otp.length !== 6) {
            otpError.textContent = t('otp_error');
            otpError.classList.remove('hidden');
            return;
        }

        otpError.classList.add('hidden');
        btnConnect.disabled = true;
        btnConnect.classList.remove('pulse-ready');
        btnConnect.querySelector('span').textContent = t('status_requesting_mic');
        setConnectionStatus(false, t('status_requesting_mic'), t('status_waiting_perm'));

        // Short user prompt to ensure browser permission dialog pops up smoothly
        showToast(t('toast_perm_prompt'));

        try {
            const hasMic = await initAudio(selectedMicId);
            btnConnect.querySelector('span').textContent = t('status_connecting');
            setConnectionStatus(false, t('status_connecting'), 'Memproses sesi audio');

            if (!hasMic) {
                showToast(t('toast_listen_only'));
            }

            const loc = window.location;
            const wsProtocol = loc.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = `${wsProtocol}//${loc.host}/ws/voice?otp=${encodeURIComponent(otp)}`;

            socket = new WebSocket(wsUrl);
            socket.binaryType = 'arraybuffer';

            socket.onopen = () => {
                setConnectionStatus(true, t('status_online'), hasMic ? t('status_connected_48k') : t('status_listen_48k'));
                startPingKeepalive();
            };

            socket.onmessage = (event) => {
                if (typeof event.data === 'string') {
                    handleJsonMessage(JSON.parse(event.data));
                } else if (event.data instanceof ArrayBuffer) {
                    handleBinaryAudioPacket(event.data);
                }
            };

            socket.onclose = (event) => {
                handleSocketClose(event);
            };

            socket.onerror = (err) => {
                console.error('WebSocket Error:', err);
            };
        } catch (err) {
            console.error('Connection failed:', err);
            btnConnect.disabled = false;
            btnConnect.querySelector('span').textContent = t('btn_connect');
            setConnectionStatus(false, t('status_failed'), 'Gagal menghubungkan sesi');
            otpError.textContent = 'Gagal menghubungkan: ' + err.message;
            otpError.classList.remove('hidden');
            showToast('Gagal menghubungkan: ' + err.message);
        }
    }

    function setConnectionStatus(connected, title, subtitle) {
        if (connected) {
            statusDot.className = 'status-indicator indicator-online';
        } else {
            statusDot.className = 'status-indicator indicator-offline';
        }
        statusTitle.textContent = title;
        statusSubtitle.textContent = subtitle;
    }

    function handleSocketClose(event) {
        clearInterval(pingInterval);
        if (vuAnimId) {
            cancelAnimationFrame(vuAnimId);
            vuAnimId = null;
        }
        if (vuBar) vuBar.style.width = '0%';
        if (vuValueText) vuValueText.textContent = '0%';

        setConnectionStatus(false, t('status_offline'), t('status_not_connected'));
        loginSection.classList.remove('hidden');
        dashboardSection.classList.add('hidden');
        userProfileChip.classList.add('hidden');
        pingBadge.classList.add('hidden');

        btnConnect.disabled = false;
        btnConnect.querySelector('span').textContent = t('btn_connect');

        if (event.code === 4001 || event.code === 4002 || event.code === 4003) {
            otpError.textContent = event.reason || t('otp_error');
            otpError.classList.remove('hidden');
            showToast(event.reason || t('otp_error'));
        }

        cleanupAudioNodes();
    }

    function disconnectVoice() {
        if (socket) {
            socket.close(1000, 'User disconnected manually');
            showToast(t('toast_session_dc'));
        }
    }

    function startPingKeepalive() {
        clearInterval(pingInterval);
        pingInterval = setInterval(() => {
            if (socket && socket.readyState === WebSocket.OPEN) {
                lastPingTimestamp = performance.now();
                sendJson({ type: 'ping' });
            }
        }, 15000);
    }

    function sendJson(obj) {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify(obj));
        }
    }

    // --- JSON Message Dispatcher ---
    function handleJsonMessage(msg) {
        if (!msg || !msg.type) return;

        switch (msg.type) {
            case 'auth_success':
                myUuid = msg.uuid;
                myName = msg.playerName;
                maxAudibleDistance = msg.maxDistance || 48.0;
                maxDistText.textContent = maxAudibleDistance;

                userName.textContent = myName;
                userAvatar.src = `https://mc-heads.net/avatar/${myUuid}/22`;
                userProfileChip.classList.remove('hidden');
                pingBadge.classList.remove('hidden');

                loginSection.classList.add('hidden');
                dashboardSection.classList.remove('hidden');

                applyMicAvailabilityUI();
                showToast(t('toast_welcome', { name: myName }));
                break;

            case 'position_sync':
                handlePositionSync(msg);
                break;

            case 'pong':
                if (lastPingTimestamp > 0) {
                    const latency = Math.round(performance.now() - lastPingTimestamp);
                    pingValue.textContent = latency;
                }
                break;

            case 'error':
                otpError.textContent = msg.message || 'Terjadi kesalahan.';
                otpError.classList.remove('hidden');
                showToast(msg.message || 'Terjadi kesalahan');
                break;

            default:
                break;
        }
    }

    function applyMicAvailabilityUI() {
        const liveIndicator = document.querySelector('.live-indicator');
        const dashNotice = document.getElementById('dashMicWarningNotice');

        if (!isMicAvailable) {
            if (liveIndicator) {
                liveIndicator.classList.add('listen-only');
                const label = liveIndicator.querySelector('span:last-child');
                if (label) label.textContent = t('badge_listen_only');
            }
            if (dashNotice) {
                dashNotice.classList.remove('hidden');
            }
            if (vuValueText) {
                vuValueText.textContent = 'Mic Terblokir';
            }
            if (btnMute) btnMute.disabled = true;
            if (btnPtt) btnPtt.disabled = true;
            if (modeVad) modeVad.disabled = true;
            if (modePtt) modePtt.disabled = true;
        } else {
            if (liveIndicator) {
                liveIndicator.classList.remove('listen-only');
                const label = liveIndicator.querySelector('span:last-child');
                if (label) label.textContent = t('dash_live_mic');
            }
            if (dashNotice) {
                dashNotice.classList.add('hidden');
            }
            if (btnMute) btnMute.disabled = false;
            if (btnPtt) btnPtt.disabled = false;
            if (modeVad) modeVad.disabled = false;
            if (modePtt) modePtt.disabled = false;
        }
        updateMicBadge();
    }

    // --- 3D Spatial Audio & Voice Group Listener ---
    function handlePositionSync(data) {
        if (!data || !data.self || !audioContext) return;

        const self = data.self;
        coordX.textContent = self.x.toFixed(1);
        coordY.textContent = self.y.toFixed(1);
        coordZ.textContent = self.z.toFixed(1);
        if (self.world) worldBadge.textContent = self.world;

        // Voice Group Status Card
        if (data.group && data.group.name) {
            if (voiceGroupCard) {
                voiceGroupCard.classList.remove('hidden');
                groupNameText.textContent = data.group.name;
                groupTypeText.textContent = `${data.group.type || 'NORMAL'} • ${t('group_active')}`;
            }
        } else {
            if (voiceGroupCard) {
                voiceGroupCard.classList.add('hidden');
            }
        }

        // Calculate Listener Forward Vector from Minecraft Yaw and Pitch
        const yawRad = (self.yaw * Math.PI) / 180.0;
        const pitchRad = (self.pitch * Math.PI) / 180.0;

        const forwardX = -Math.sin(yawRad) * Math.cos(pitchRad);
        const forwardY = -Math.sin(pitchRad);
        const forwardZ = Math.cos(yawRad) * Math.cos(pitchRad);

        const listener = audioContext.listener;
        const ct = audioContext.currentTime;

        if (listener.positionX) {
            listener.positionX.setValueAtTime(self.x, ct);
            listener.positionY.setValueAtTime(self.y, ct);
            listener.positionZ.setValueAtTime(self.z, ct);
            listener.forwardX.setValueAtTime(forwardX, ct);
            listener.forwardY.setValueAtTime(forwardY, ct);
            listener.forwardZ.setValueAtTime(forwardZ, ct);
            listener.upX.setValueAtTime(0, ct);
            listener.upY.setValueAtTime(1, ct);
            listener.upZ.setValueAtTime(0, ct);
        } else {
            listener.setPosition(self.x, self.y, self.z);
            listener.setOrientation(forwardX, forwardY, forwardZ, 0, 1, 0);
        }

        nearbyPlayersCache = data.nearby || [];
        renderNearbyPlayers(nearbyPlayersCache);
    }

    // --- Audio Packet Playback (Proximity 0x01 & Group 0x03) ---
    function handleBinaryAudioPacket(arrayBuffer) {
        if (!audioContext || isDeafened) return;
        if (arrayBuffer.byteLength < 33) return;

        const view = new DataView(arrayBuffer);
        const packetType = view.getUint8(0);

        if (packetType === 0x01) {
            // 0x01 = Proximity 3D HRTF audio
            if (arrayBuffer.byteLength < 1961) return;

            const msb = view.getBigInt64(1, false);
            const lsb = view.getBigInt64(9, false);
            const senderUuid = bigIntsToUuid(msb, lsb);

            if (senderUuid === myUuid) return;

            const x = view.getFloat64(17, true);
            const y = view.getFloat64(25, true);
            const z = view.getFloat64(33, true);

            const sampleCount = 960;
            const floatSamples = new Float32Array(sampleCount);
            let offset = 41;
            for (let i = 0; i < sampleCount; i++, offset += 2) {
                const rawInt16 = view.getInt16(offset, true);
                floatSamples[i] = rawInt16 < 0 ? rawInt16 / 32768.0 : rawInt16 / 32767.0;
            }

            const speaker = getOrCreateSpeaker(senderUuid);

            // Update Panner 3D Position
            const ct = audioContext.currentTime;
            if (speaker.panner.positionX) {
                speaker.panner.positionX.setValueAtTime(x, ct);
                speaker.panner.positionY.setValueAtTime(y, ct);
                speaker.panner.positionZ.setValueAtTime(z, ct);
            } else {
                speaker.panner.setPosition(x, y, z);
            }

            const audioBuffer = audioContext.createBuffer(1, sampleCount, 48000);
            audioBuffer.getChannelData(0).set(floatSamples);

            const source = audioContext.createBufferSource();
            source.buffer = audioBuffer;
            source.connect(speaker.panner);

            if (!speaker.nextStartTime || speaker.nextStartTime < ct) {
                speaker.nextStartTime = ct + 0.04;
            }
            source.start(speaker.nextStartTime);
            speaker.nextStartTime += sampleCount / 48000.0;

            highlightSpeaker(senderUuid);

        } else if (packetType === 0x03) {
            // 0x03 = Group Voice audio (non-positional centered stereo, no distance rolloff)
            if (arrayBuffer.byteLength < 1953) return;

            const msb = view.getBigInt64(1, false);
            const lsb = view.getBigInt64(9, false);
            const senderUuid = bigIntsToUuid(msb, lsb);

            if (senderUuid === myUuid) return;

            const sampleCount = 960;
            const floatSamples = new Float32Array(sampleCount);
            let offset = 33;
            for (let i = 0; i < sampleCount; i++, offset += 2) {
                const rawInt16 = view.getInt16(offset, true);
                floatSamples[i] = rawInt16 < 0 ? rawInt16 / 32768.0 : rawInt16 / 32767.0;
            }

            const groupSpeaker = getOrCreateGroupSpeaker(senderUuid);

            const audioBuffer = audioContext.createBuffer(1, sampleCount, 48000);
            audioBuffer.getChannelData(0).set(floatSamples);

            const source = audioContext.createBufferSource();
            source.buffer = audioBuffer;
            source.connect(groupSpeaker.gain); // Bypasses PannerNode, connects direct to gain!

            const ct = audioContext.currentTime;
            if (!groupSpeaker.nextStartTime || groupSpeaker.nextStartTime < ct) {
                groupSpeaker.nextStartTime = ct + 0.04;
            }
            source.start(groupSpeaker.nextStartTime);
            groupSpeaker.nextStartTime += sampleCount / 48000.0;

            highlightSpeaker(senderUuid);
        }
    }

    function getOrCreateSpeaker(uuid) {
        let speaker = speakers.get(uuid);
        if (speaker) return speaker;

        const panner = audioContext.createPanner();
        panner.panningModel = 'HRTF';
        panner.distanceModel = 'linear';
        panner.refDistance = 2.0;
        panner.maxDistance = maxAudibleDistance;
        panner.rolloffFactor = 1.0;

        const gainNode = audioContext.createGain();
        const individualVol = speakerVolumes.has(uuid) ? speakerVolumes.get(uuid) : 1.0;
        gainNode.gain.value = individualVol * masterVolume;

        panner.connect(gainNode);
        gainNode.connect(audioContext.destination);

        speaker = {
            panner: panner,
            gain: gainNode,
            nextStartTime: 0,
            timeoutId: null
        };
        speakers.set(uuid, speaker);
        return speaker;
    }

    function getOrCreateGroupSpeaker(uuid) {
        let groupSpeaker = groupSpeakers.get(uuid);
        if (groupSpeaker) return groupSpeaker;

        const gainNode = audioContext.createGain();
        const individualVol = speakerVolumes.has(uuid) ? speakerVolumes.get(uuid) : 1.0;
        gainNode.gain.value = individualVol * masterVolume;

        gainNode.connect(audioContext.destination);

        groupSpeaker = {
            gain: gainNode,
            nextStartTime: 0,
            timeoutId: null
        };
        groupSpeakers.set(uuid, groupSpeaker);
        return groupSpeaker;
    }

    function updateAllSpeakerGains() {
        speakers.forEach((speaker, uuid) => {
            const individualVol = speakerVolumes.has(uuid) ? speakerVolumes.get(uuid) : 1.0;
            speaker.gain.gain.value = individualVol * masterVolume;
        });
        groupSpeakers.forEach((speaker, uuid) => {
            const individualVol = speakerVolumes.has(uuid) ? speakerVolumes.get(uuid) : 1.0;
            speaker.gain.gain.value = individualVol * masterVolume;
        });
    }

    function highlightSpeaker(uuid) {
        const card = document.getElementById(`player-card-${uuid}`);
        if (card) {
            card.classList.add('is-speaking');
            const speaker = speakers.get(uuid) || groupSpeakers.get(uuid);
            if (speaker) {
                clearTimeout(speaker.timeoutId);
                speaker.timeoutId = setTimeout(() => {
                    card.classList.remove('is-speaking');
                }, 280);
            }
        }
    }

    // --- Render Nearby Players ---
    function renderNearbyPlayers(players) {
        nearbyCount.textContent = players.length;

        if (players.length === 0) {
            nearbyPlayersList.innerHTML = `
                <div class="empty-players">
                    <span class="empty-glyph">◈</span>
                    <strong>${t('nearby_empty_title')}</strong>
                    <p>${t('nearby_empty_desc')}</p>
                </div>
            `;
            return;
        }

        let html = '';
        for (const p of players) {
            const vol = speakerVolumes.has(p.uuid) ? Math.round(speakerVolumes.get(p.uuid) * 100) : 100;
            const groupBadgeHtml = p.inGroup ? `<span class="badge-group">GROUP</span>` : '';
            html += `
                <div class="player-card" id="player-card-${p.uuid}">
                    <div class="player-card-top">
                        <div class="player-identity">
                            <img class="player-avatar" src="https://mc-heads.net/avatar/${p.uuid}/24" alt="${escapeHtml(p.name)}" onerror="this.src='data:image/svg+xml,%3Csvg xmlns=\\'http://www.w3.org/2000/svg\\' viewBox=\\'0 0 24 24\\'%3E%3C/svg%3E'">
                            <span class="player-name">${escapeHtml(p.name)}</span>
                            ${groupBadgeHtml}
                        </div>
                        <span class="player-distance">${p.distance}m</span>
                    </div>
                    <div class="player-vol-row">
                        <span>Vol:</span>
                        <input type="range" min="0" max="200" value="${vol}" class="green-slider slider-mini player-vol-slider" data-uuid="${p.uuid}" title="Volume ${escapeHtml(p.name)}">
                    </div>
                </div>
            `;
        }
        nearbyPlayersList.innerHTML = html;

        const sliders = nearbyPlayersList.querySelectorAll('.player-vol-slider');
        sliders.forEach(slider => {
            slider.addEventListener('input', (e) => {
                const targetUuid = e.target.getAttribute('data-uuid');
                const newVol = parseInt(e.target.value, 10) / 100.0;
                speakerVolumes.set(targetUuid, newVol);

                const speaker = speakers.get(targetUuid);
                if (speaker) {
                    speaker.gain.gain.value = newVol * masterVolume;
                }
                const grpSpeaker = groupSpeakers.get(targetUuid);
                if (grpSpeaker) {
                    grpSpeaker.gain.gain.value = newVol * masterVolume;
                }
            });
        });
    }

    function cleanupAudioNodes() {
        speakers.forEach(speaker => {
            try {
                speaker.gain.disconnect();
                speaker.panner.disconnect();
            } catch (ignored) {}
        });
        speakers.clear();

        groupSpeakers.forEach(speaker => {
            try {
                speaker.gain.disconnect();
            } catch (ignored) {}
        });
        groupSpeakers.clear();
    }

    function bigIntsToUuid(msb, lsb) {
        const hex = (msb.toString(16).padStart(16, '0') + lsb.toString(16).padStart(16, '0')).toLowerCase();
        return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, (m) => {
            switch (m) {
                case '&': return '&amp;';
                case '<': return '&lt;';
                case '>': return '&gt;';
                case '"': return '&quot;';
                case "'": return '&#039;';
                default: return m;
            }
        });
    }
})();