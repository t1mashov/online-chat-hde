package com.example.online_chat_hde.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.online_chat_hde.R
import com.example.online_chat_hde.core.DateKeys
import com.example.online_chat_hde.core.ErrorKeys

open class ChatUIConfig(
    var colors: ChatUIColors,
    var media: ChatUIMedia,
    var dimensions: ChatUIDimensions,
    var texts: ChatUITexts,
)

data class ChatUIMedia(
    var noStaffLogo: Int,
    var linkFileLogo: Int,
    var sendMessageLogo: Int,
    var closeChatLogo: Int,
    var ticketLogo: Int,
    var ticketNameLogo: Int,
    var ticketEmailLogo: Int,
)

data class ChatUIColors(
    var background: Color,
    var loadingBackground: Color,
    var topPanelBackground: Color,
    var bottomPanelBackground: Color,
    var topPanelText: Color,
    var closeChatButtonBackground: Color,
    var closeChatButtonIcon: Color,

    var userMessageBackground: Color,
    var userMessageText: Color,
    var userRipple: Color,
    var userTimeText: Color,

    var serverMessageBackground: Color,
    var serverMessageAgent: Color,
    var serverMessageText: Color,
    var serverRipple: Color,
    var serverTimeText: Color,

    var virtualMessageBackground: Color,
    var virtualMessageAgent: Color,
    var virtualMessageText: Color,
    var virtualRipple: Color,
    var virtualTimeText: Color,

    var topDivider: Color,
    var bottomDivider: Color,
    var timeOnImageText: Color,
    var timeOnImageBackground: Color,
    var userLoadingImageColor: Color,
    var pyperclip: Color,
    var sendMessage: Color,
    var messagePlaceholder: Color,
    var buttonBackground: Color,
    var buttonText: Color,
    var buttonRipple: Color,
    var imageRipple: Color,
    var globalLoading: Color,
    var errorPrimary: Color,
    var errorSecondary: Color,
    var ticketFieldDisabled: Color,
    var ticketSubText: Color,
    var statusBarBackground: Color,
    var navigationBarBackground: Color,
    var showPrependMessagesBackground: Color,
    var dateText: Color
)

data class ChatUIDimensions(
    /** Отступы у верхней панели */
    var topPanelPadding: Dp,

    /** Отступы у контента по горизонтали */
    var contentHorizontalPadding: Dp,

    /** Отступы у нижней панели сообщений */
    var bottomPanelPadding: Dp,

    /** Внутренние отступы у сообщения */
    var messagePadding: Dp,

    /** Отступ между сообщениями (по вертикали) */
    var messageIndent: Dp,

    /** Размер шрифта в тексте сообщений */
    var messageFontSize: TextUnit,

    /** Размер шрифта имени агента */
    var agentNameFontSize: TextUnit,

    /** Размер шрифта подписи времени */
    var timeFontSize: TextUnit,

    /** Отступы между элементами внутри сообщения */
    var innerIndent: Dp,
    /** Размер иконки скрепки */
    var pyperclipSize: Dp,

    /** Скругления углов у сообщения (текстового) пользователя */
    var userTextMessagesCorners: RoundedCornerShape,
    /** Скругления углов у сообщения (файла) пользователя */
    var userFileMessagesCorners: RoundedCornerShape,
    /** Скругления углов у сообщения (изображения) пользователя */
    var userImageMessagesCorners: RoundedCornerShape,

    /** Размер изображения отправленного пользователем */
    var userImageMessageSize: Dp,

    /** Размер изображения отправленного агентом */
    var serverImageMessageSize: Dp,

    /** Скругления углов у сообщения агента (текстового) */
    var serverTextMessagesCorners: RoundedCornerShape,
    /** Скругления углов у сообщения сервера (файла) */
    var serverFileMessagesCorners: RoundedCornerShape,
    /** Скругления углов у сообщения сервера (изображения) */
    var serverImageMessagesCorners: RoundedCornerShape,

    /** Углы заднего фона у подписи времени отправления сообщения на картинках */
    var timeOnImageCorners: RoundedCornerShape,

    /** Минимальный отступ от края экрана до сообщения (слева для сообщений пользователя и справа для ответов сервера) */
    var messageMinEndIndent: Dp,

    /** Размер иконки загрузки напротив сообщения */
    var messageLoadingIconSize: Dp,

    /** Размер иконок элементов управления */
    var logoSize: Dp,

    /** Скругления углов кнопок в сообщении и тикете */
    var buttonCorners: RoundedCornerShape,

    /** Внутренний отступ у кнопок сообщения и тикета */
    var buttonPadding: Dp,

    /** Размер иконок выбора действия при загрузке документа */
    var bottomSheetIconSize: Dp,

    /** Скругления углов у аватара агента */
    var staffIconCorners: RoundedCornerShape,

    /** Отступ у полей для ввода в тикете */
    var ticketFieldsIndent: Dp,

    /** Размер иконки загрузки чата */
    var loadingLogoSize: Dp,

    /** Размер вспомогательного текста в тикете */
    var ticketSubFontSize: TextUnit,

    /**  */
    var ticketFieldsCorners: RoundedCornerShape,
)

data class ChatUITexts(
    var unassigned: String,
    var messagePlaceholder: String,
    var connectionError: String,
    var showPrependMessages: String,
    var ticketTitle: String,
    var ticketOffline: String,
    var ticketNamePlaceholder: String,
    var ticketEmailPlaceholder: String,
    var ticketFormPlaceholder: String,
    var ticketPolicy: String,
    var ticketPolicyLink: String,
    var ticketSend: String,
    var emptyContentError: String,
    var emptyNameError: String,
    var emptyEmailError: String,
    var invalidEmailError: String,
    var unacceptedConsentError: String,
    var waitForReply: String,
    var uploadError: String,
    var errorImageLoading: String,

    var errors: Map<String, String>,

    var months: Map<String, String>
)


object ChatUIConfigDefault : ChatUIConfig(
    colors = ChatUIColors(
        background = Color(0xFFEEEEEE),
        topPanelBackground = Color(0xFF226C78),
        topPanelText = Color(0xFFFFFFFF),
        closeChatButtonBackground = Color(0xFF13424B),
        closeChatButtonIcon = Color(0xFFDAE3E5),

        userMessageBackground = Color(0xFF297D8B),
        userMessageText = Color(0xFFFFFFFF),
        userRipple = Color(0xFFffffff),
        userTimeText = Color(0xD2FFFFFF),

        serverMessageBackground = Color(0xFFE1E7EC),
        serverMessageAgent = Color(0xFF000000),
        serverMessageText = Color(0xFF343434),
        serverRipple = Color(0xFF000000),
        serverTimeText = Color(0xD24B4B4B),

        virtualMessageBackground = Color(0xFFC9E8E2),
        virtualMessageAgent = Color(0xFF000000),
        virtualMessageText = Color(0xFF343434),
        virtualRipple = Color(0xFF000000),
        virtualTimeText = Color(0xD24B4B4B),

        topDivider = Color(0xFFFFFFFF),
        bottomDivider = Color(0xFFBABABA),
        timeOnImageText = Color(0xCBFFFFFF),
        timeOnImageBackground = Color(0x906C6C6C),
        userLoadingImageColor = Color(0xFFA8BEC2),
        bottomPanelBackground = Color(0xFFF3F3F3),
        pyperclip = Color(0xFF297D8B),
        sendMessage = Color(0xFF297D8B),
        messagePlaceholder = Color(0xFF787878),
        buttonBackground = Color(0xFF19545E),
        buttonText = Color(0xFFFFFFFF),
        buttonRipple = Color(0xFFFFFFFF),
        imageRipple = Color(0xFFFFFFFF),
        globalLoading = Color(0xFFB0BABB),
        errorPrimary = Color(0xFFDC6666),
        errorSecondary = Color(0xFFFFFFFF),
        loadingBackground = Color(0x72F3F3F3),
        ticketFieldDisabled = Color(0xFFB4B4B4),
        ticketSubText = Color(0xFF181818),
        statusBarBackground = Color(0xFF19545E),
        navigationBarBackground = Color(0xFF000000),
        showPrependMessagesBackground = Color(0xFFFFFFFF),
        dateText = Color(0xFF4E4E4E),
    ),
    dimensions = ChatUIDimensions(
        topPanelPadding = 12.dp,
        contentHorizontalPadding = 8.dp,
        bottomPanelPadding = 12.dp,
        messagePadding = 8.dp,
        messageIndent = 4.dp,
        userTextMessagesCorners = RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp),
        userFileMessagesCorners = RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp),
        userImageMessagesCorners = RoundedCornerShape(8.dp),
        serverTextMessagesCorners = RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp),
        serverFileMessagesCorners = RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp),
        serverImageMessagesCorners = RoundedCornerShape(8.dp),
        timeOnImageCorners = RoundedCornerShape(12.dp),
        messageMinEndIndent = 32.dp,
        messageFontSize = 14.sp,
        timeFontSize = 10.sp,
        innerIndent = 10.dp,
        pyperclipSize = 20.dp,
        messageLoadingIconSize = 14.dp,
        userImageMessageSize = 150.dp,
        serverImageMessageSize = 150.dp,
        logoSize = 25.dp,
        buttonCorners = RoundedCornerShape(12.dp),
        buttonPadding = 8.dp,
        bottomSheetIconSize = 36.dp,
        staffIconCorners = RoundedCornerShape(16.dp),
        ticketFieldsIndent = 12.dp,
        loadingLogoSize = 56.dp,
        ticketSubFontSize = 12.sp,
        ticketFieldsCorners = RoundedCornerShape(8.dp),
        agentNameFontSize = 12.sp
    ),
    texts = ChatUITexts(
        unassigned = "Пожалуйста, подождите",
        messagePlaceholder = "Сообщение",
        connectionError = "Нет подключения к Интернету",
        showPrependMessages = "Показать предыдущие сообщения",
        ticketTitle = "Оставьте нам Ваш вопрос!",
        ticketOffline = "К сожалению нет операторов в сети. Отправьте Ваше сообщение и мы обязательно ответим на него на Вашу э-почту.",
        ticketNamePlaceholder = "Имя",
        ticketEmailPlaceholder = "Э-почта",
        ticketFormPlaceholder = "Отправить Ваш вопрос",
        ticketPolicy = "Согласие на обработку перс. данных",
        ticketPolicyLink = "(Ссылка)",
        ticketSend = "Отправить",
        emptyContentError = "Введите Ваш вопрос",
        emptyNameError = "Укажите Ваше имя",
        emptyEmailError = "Укажите Вашу э-почту",
        unacceptedConsentError = "Необходимо согласие",
        invalidEmailError = "Неверный формат э-почты",
        waitForReply = "Спасибо за Ваше сообщение!\nМы скоро на него  ответим!",
        uploadError = "Не удалось загрузить файл, попробуйте загрузить повторно",
        errorImageLoading = "Не удалось загрузить изображение",
        errors = mapOf(
            ErrorKeys.FILE_MAX_SIZE to "Превышен максимальный размер загружаемого файла"
        ),
        months = mapOf(
            DateKeys.TODAY to "Сегодня",

            DateKeys.MONTHS_JANUARY to "Январь",
            DateKeys.MONTHS_FEBRUARY to "Февраль",
            DateKeys.MONTHS_MARCH to "Март",
            DateKeys.MONTHS_APRIL to "Апрель",
            DateKeys.MONTHS_MAY to "Май",
            DateKeys.MONTHS_JUNE to "Июнь",
            DateKeys.MONTHS_JULY to "Июль",
            DateKeys.MONTHS_AUGUST to "Август",
            DateKeys.MONTHS_SEPTEMBER to "Сентябрь",
            DateKeys.MONTHS_OCTOBER to "Октябрь",
            DateKeys.MONTHS_NOVEMBER to "Ноябрь",
            DateKeys.MONTHS_DECEMBER to "Декабрь",

            DateKeys.MONTHS_PLURAL_JANUARY to "Января",
            DateKeys.MONTHS_PLURAL_FEBRUARY to "Февраля",
            DateKeys.MONTHS_PLURAL_MARCH to "Марта",
            DateKeys.MONTHS_PLURAL_APRIL to "Апреля",
            DateKeys.MONTHS_PLURAL_MAY to "Мая",
            DateKeys.MONTHS_PLURAL_JUNE to "Июня",
            DateKeys.MONTHS_PLURAL_JULY to "Июля",
            DateKeys.MONTHS_PLURAL_AUGUST to "Августа",
            DateKeys.MONTHS_PLURAL_SEPTEMBER to "Сентября",
            DateKeys.MONTHS_PLURAL_OCTOBER to "Октября",
            DateKeys.MONTHS_PLURAL_NOVEMBER to "Ноября",
            DateKeys.MONTHS_PLURAL_DECEMBER to "Декабря",
        )
    ),
    media = ChatUIMedia(
        noStaffLogo = R.drawable.headspeakers,
        linkFileLogo = R.drawable.piperclip,
        sendMessageLogo = R.drawable.send,
        closeChatLogo = R.drawable.close,
        ticketLogo = R.drawable.message,
        ticketNameLogo = R.drawable.name,
        ticketEmailLogo = R.drawable.email
    )
)