package com.example.blogmultiplatform.pages.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.blogmultiplatform.components.AdminPageLayout
import com.example.blogmultiplatform.components.ControlPopup
import com.example.blogmultiplatform.components.MessagePopup
import com.example.blogmultiplatform.models.ApiResponse
import com.example.blogmultiplatform.models.Constants.POST_ID_PARAM
import com.example.blogmultiplatform.models.ControlStyle
import com.example.blogmultiplatform.models.EditorControl
import com.example.blogmultiplatform.models.Post
import com.example.blogmultiplatform.navigation.Screen
import com.example.blogmultiplatform.styles.EditorKeyStyle
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.example.blogmultiplatform.util.Constants.SIDE_PANEL_WIDTH
import com.example.blogmultiplatform.util.Id
import com.example.blogmultiplatform.util.addPost
import com.example.blogmultiplatform.util.applyControlStyle
import com.example.blogmultiplatform.util.applyStyle
import com.example.blogmultiplatform.util.fetchSelectedPost
import com.example.blogmultiplatform.util.getSelectedText
import com.example.blogmultiplatform.util.isUserLoggedIn
import com.example.blogmultiplatform.util.noBorder
import com.example.blogmultiplatform.util.updatePost
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.Resize
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.Visibility
import com.varabyte.kobweb.browser.file.loadDataUrlFromDisk
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.disabled
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.onKeyDown
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.resize
import com.varabyte.kobweb.compose.ui.modifiers.scrollBehavior
import com.varabyte.kobweb.compose.ui.modifiers.visibility
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.silk.components.forms.SwitchSize
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.get
import kotlin.js.Date
import com.example.shared.Category
import com.example.shared.JsTheme
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier

data class CreatePageUiState(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var thumbnail: String = "https://via.placeholder.com/400x200",
    // true = user wants to paste a URL; false = user will upload a file
    var thumbnailUseUrl: Boolean = true,
    var content: String = "",
    var category: Category = Category.Technology,
    var buttonText: String = "Create",
    var popular: Boolean = false,
    var main: Boolean = false,
    var sponsored: Boolean = false,
    var editorVisibility: Boolean = true,
    var messagePopup: Boolean = false,
    var linkPopup: Boolean = false,
    var imagePopup: Boolean = false
) {
    fun reset() = this.copy(
        id = "",
        title = "",
        subtitle = "",
        thumbnail = "",
        content = "",
        category = Category.Programming,
        buttonText = "Create",
        main = false,
        popular = false,
        sponsored = false,
        editorVisibility = true,
        messagePopup = false,
        linkPopup = false,
        imagePopup = false
    )
}

@Page("/admin/create")
@Composable
fun CreatePage() {
    isUserLoggedIn {
        CreateScreen()
    }
}

@Composable
fun CreateScreen() {
    val scope = rememberCoroutineScope()
    val context = rememberPageContext()
    val breakpoint = rememberBreakpoint()
    var uiState by remember { mutableStateOf(CreatePageUiState()) }

    val hasPostIdParam = remember(key1 = context.route) {
        context.route.params.containsKey(POST_ID_PARAM)
    }

    LaunchedEffect(hasPostIdParam) {
        if (hasPostIdParam) {
            val postId = context.route.params[POST_ID_PARAM] ?: ""
            val response = fetchSelectedPost(id = postId)
            if (response is ApiResponse.Success) {
                // Set state instead of writing to DOM directly
                uiState = uiState.copy(
                    id = response.data._id,
                    title = response.data.title,
                    subtitle = response.data.subtitle,
                    content = response.data.content,
                    category = response.data.category,
                    thumbnail = response.data.thumbnail,
                    buttonText = "Update",
                    main = response.data.main,
                    popular = response.data.popular,
                    sponsored = response.data.sponsored
                )
                // Update the actual DOM element values so the inputs reflect the new state
                try {
                    (document.getElementById(Id.titleInput) as? HTMLInputElement)?.value = response.data.title
                    (document.getElementById(Id.subtitleInput) as? HTMLInputElement)?.value = response.data.subtitle
                    (document.getElementById(Id.thumbnailInput) as? HTMLInputElement)?.value = response.data.thumbnail
                    (document.getElementById(Id.editor) as? HTMLTextAreaElement)?.value = response.data.content
                    document.getElementById(Id.editorPreview)?.innerHTML = response.data.content
                } catch (e: Throwable) {
                    console.log("DOM update error:", e.message)
                }
            }
        } else {
            // Clear state instead of touching DOM
            uiState = uiState.reset()
            // Clear DOM values as well
            try {
                (document.getElementById(Id.titleInput) as? HTMLInputElement)?.value = ""
                (document.getElementById(Id.subtitleInput) as? HTMLInputElement)?.value = ""
                (document.getElementById(Id.thumbnailInput) as? HTMLInputElement)?.value = ""
                (document.getElementById(Id.editor) as? HTMLTextAreaElement)?.value = ""
                document.getElementById(Id.editorPreview)?.innerHTML = ""
            } catch (e: Throwable) {
                console.log("DOM clear error:", e.message)
            }
        }
    }

    AdminPageLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .margin(topBottom = 50.px)
                .padding(left = if (breakpoint > Breakpoint.MD) SIDE_PANEL_WIDTH.px else 0.px),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .maxWidth(700.px),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleGrid(numColumns = numColumns(base = 1, sm = 3)) {
                    Row(
                        modifier = Modifier
                            .margin(
                                right = 24.px,
                                bottom = if (breakpoint < Breakpoint.SM) 12.px else 0.px
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            modifier = Modifier.margin(right = 8.px),
                            checked = uiState.popular,
                            onCheckedChange = { uiState = uiState.copy(popular = it) },
                            size = SwitchSize.LG
                        )
                        SpanText(
                            modifier = Modifier
                                .fontSize(14.px)
                                .fontFamily(FONT_FAMILY)
                                .color(JsTheme.HalfBlack.rgb),
                            text = "Popular"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .margin(
                                right = 24.px,
                                bottom = if (breakpoint < Breakpoint.SM) 12.px else 0.px
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            modifier = Modifier.margin(right = 8.px),
                            checked = uiState.main,
                            onCheckedChange = { uiState = uiState.copy(main = it) },
                            size = SwitchSize.LG
                        )
                        SpanText(
                            modifier = Modifier
                                .fontSize(14.px)
                                .fontFamily(FONT_FAMILY)
                                .color(JsTheme.HalfBlack.rgb),
                            text = "Main"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .margin(bottom = if (breakpoint < Breakpoint.SM) 12.px else 0.px),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            modifier = Modifier.margin(right = 8.px),
                            checked = uiState.sponsored,
                            onCheckedChange = { uiState = uiState.copy(sponsored = it) },
                            size = SwitchSize.LG
                        )
                        SpanText(
                            modifier = Modifier
                                .fontSize(14.px)
                                .fontFamily(FONT_FAMILY)
                                .color(JsTheme.HalfBlack.rgb),
                            text = "Sponsored"
                        )
                    }
                }
                Input(
                    type = InputType.Text,
                    attrs = Modifier
                        .id(Id.titleInput)
                        .fillMaxWidth()
                        .height(54.px)
                        .margin(topBottom = 12.px)
                        .padding(leftRight = 20.px)
                        .backgroundColor(JsTheme.LightGray.rgb)
                        .borderRadius(r = 4.px)
                        .noBorder()
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .toAttrs {
                            attr("placeholder", "Title")
                            // Keep value as an attribute so the input reflects state changes
                            // Use the Compose-controlled value API so the input updates with state
                            value(uiState.title)
                             onInput { event ->
                                console.log("Title input change ->", event.value)
                                uiState = uiState.copy(title = event.value)
                            }
                        }
                )
                Input(
                    type = InputType.Text,
                    attrs = Modifier
                        .id(Id.subtitleInput)
                        .fillMaxWidth()
                        .height(54.px)
                        .padding(leftRight = 20.px)
                        .backgroundColor(JsTheme.LightGray.rgb)
                        .borderRadius(r = 4.px)
                        .noBorder()
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .toAttrs {
                            attr("placeholder", "Subtitle")
                            // Keep value as an attribute so the input reflects state changes
                            // Use the Compose-controlled value API so the input updates with state
                            value(uiState.subtitle)
                             onInput { event ->
                                console.log("Subtitle input change ->", event.value)
                                uiState = uiState.copy(subtitle = event.value)
                            }
                        }
                )
                CategoryDropdown(
                    selectedCategory = uiState.category,
                    onCategorySelect = {
                        console.log("Selected category:", it.name)
                        uiState = uiState.copy(category = it)
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().margin(topBottom = 12.px),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        modifier = Modifier.margin(right = 8.px),
                        // When checked = true, user wants to paste a URL
                        checked = uiState.thumbnailUseUrl,
                        onCheckedChange = {
                            console.log("Thumbnail use URL toggled ->", it)
                            uiState = uiState.copy(thumbnailUseUrl = it)
                        },
                        size = SwitchSize.MD
                    )
                    SpanText(
                        modifier = Modifier
                            .fontSize(14.px)
                            .fontFamily(FONT_FAMILY)
                            .color(JsTheme.HalfBlack.rgb),
                        text = "Paste an Image URL instead"
                    )
                }
                ThumbnailUploader(
                    thumbnail = uiState.thumbnail,
                    // pass disabled = not using URL (i.e., upload mode when false)
                    thumbnailInputDisabled = !uiState.thumbnailUseUrl,
                    onThumbnailSelect = { filename, file ->
                        (document.getElementById(Id.thumbnailInput) as HTMLInputElement).value =
                            filename
                        uiState = uiState.copy(thumbnail = file)
                    }
                )
                EditorControls(
                    breakpoint = breakpoint,
                    editorVisibility = uiState.editorVisibility,
                    editorContent = uiState.content,
                    onEditorVisibilityChange = { content ->
                        uiState = uiState.copy(editorVisibility = !uiState.editorVisibility)
                        document.getElementById(Id.editorPreview)?.innerHTML = content
                        try {
                            js("hljs.highlightAll()") as Unit
                        } catch (e: Throwable) {
                            console.log("Highlight error: ${e.message}")
                        }
                    },
                     onLinkClick = {
                         uiState = uiState.copy(linkPopup = true)
                     },
                     onImageClick = {
                         uiState = uiState.copy(imagePopup = true)
                     }
                 )
                Editor(
                    editorVisibility = uiState.editorVisibility,
                    content = uiState.content,
                    onContentChange = { content ->
                        uiState = uiState.copy(content = content)
                    }
                )
                CreateButton(
                    text = uiState.buttonText,
                    onClick = {
                        console.log("Create button clicked")
                        console.log("Current state:", uiState)

                        // Get the latest content from the editor
                        val editorContent = try {
                            (document.getElementById(Id.editor) as? HTMLTextAreaElement)?.value ?: uiState.content
                        } catch (e: Exception) {
                            console.log("Error getting editor content:", e.message)
                            uiState.content
                        }

                        // Update content from editor
                        val finalContent = if (editorContent.isNotEmpty()) editorContent else uiState.content
                        uiState = uiState.copy(content = finalContent)

                        console.log("Form validation:")
                        console.log("Title: '${uiState.title}'")
                        console.log("Subtitle: '${uiState.subtitle}'")
                        console.log("Thumbnail: '${uiState.thumbnail}'")
                        console.log("Content: '${finalContent}'")

                        if (
                            uiState.title.isNotEmpty() &&
                            uiState.subtitle.isNotEmpty() &&
                            uiState.thumbnail.isNotEmpty() &&
                            finalContent.isNotEmpty()
                        ) {
                            console.log("Form validation passed, submitting...")
                            scope.launch {
                                try {
                                    if (hasPostIdParam) {
                                        console.log("Updating existing post...")
                                        val result = updatePost(
                                            Post(
                                                _id = uiState.id,
                                                title = uiState.title,
                                                subtitle = uiState.subtitle,
                                                thumbnail = uiState.thumbnail,
                                                content = finalContent,
                                                category = uiState.category,
                                                popular = uiState.popular,
                                                main = uiState.main,
                                                sponsored = uiState.sponsored
                                            )
                                        )
                                        if (result) {
                                            console.log("Post updated successfully")
                                            context.router.navigateTo(Screen.AdminSuccess.postUpdated())
                                        } else {
                                            console.log("Failed to update post")
                                            uiState = uiState.copy(messagePopup = true)
                                        }
                                    } else {
                                        console.log("Creating new post...")
                                        val result = addPost(
                                            Post(
                                                author = localStorage["username"] ?: "Unknown",
                                                title = uiState.title,
                                                subtitle = uiState.subtitle,
                                                date = Date.now(),
                                                thumbnail = uiState.thumbnail,
                                                content = finalContent,
                                                category = uiState.category,
                                                popular = uiState.popular,
                                                main = uiState.main,
                                                sponsored = uiState.sponsored
                                            )
                                        )
                                        if (result) {
                                            console.log("Post created successfully")
                                            context.router.navigateTo(Screen.AdminSuccess.route)
                                        } else {
                                            console.log("Failed to create post")
                                            uiState = uiState.copy(messagePopup = true)
                                        }
                                    }
                                } catch (e: Exception) {
                                    console.log("Error during post submission:", e.message)
                                    uiState = uiState.copy(messagePopup = true)
                                }
                            }
                        } else {
                            console.log("Form validation failed")
                            val missingFields = mutableListOf<String>()
                            if (uiState.title.isEmpty()) missingFields.add("Title")
                            if (uiState.subtitle.isEmpty()) missingFields.add("Subtitle")
                            if (uiState.thumbnail.isEmpty()) missingFields.add("Thumbnail")
                            if (finalContent.isEmpty()) missingFields.add("Content")

                            console.log("Missing fields: ${missingFields.joinToString(", ")}")
                            scope.launch {
                                uiState = uiState.copy(messagePopup = true)
                                delay(3000)
                                uiState = uiState.copy(messagePopup = false)
                            }
                        }
                    }
                )
            }
        }
    }
    if (uiState.messagePopup) {
        MessagePopup(
            message = "Please fill out all required fields: Title, Subtitle, Thumbnail, and Content.",
            onDialogDismiss = { uiState = uiState.copy(messagePopup = false) }
        )
    }
    if (uiState.linkPopup) {
        ControlPopup(
            editorControl = EditorControl.Link,
            onDialogDismiss = { uiState = uiState.copy(linkPopup = false) },
            onAddClick = { href, title ->
                applyStyle(
                    ControlStyle.Link(
                        selectedText = getSelectedText(),
                        href = href,
                        title = title
                    )
                )
            }
        )
    }
    if (uiState.imagePopup) {
        ControlPopup(
            editorControl = EditorControl.Image,
            onDialogDismiss = { uiState = uiState.copy(imagePopup = false) },
            onAddClick = { imageUrl, description ->
                applyStyle(
                    ControlStyle.Image(
                        selectedText = getSelectedText(),
                        imageUrl = imageUrl,
                        alt = description
                    )
                )
            }
        )
    }
    // Place sticky bar so the button is always accessible
    StickyCreateBar(text = uiState.buttonText) {
        // reuse the same click handler as the inline CreateButton - call the existing click logic by simulating a click
        // We'll perform the same validation & submit logic inline for clarity
        console.log("Sticky Create clicked")
        val editorContent = try {
            (document.getElementById(Id.editor) as? HTMLTextAreaElement)?.value ?: uiState.content
        } catch (e: Exception) {
            console.log("Error getting editor content:", e.message)
            uiState.content
        }
        val finalContent = if (editorContent.isNotEmpty()) editorContent else uiState.content
        uiState = uiState.copy(content = finalContent)

        if (
            uiState.title.isNotEmpty() &&
            uiState.subtitle.isNotEmpty() &&
            uiState.thumbnail.isNotEmpty() &&
            finalContent.isNotEmpty()
        ) {
            scope.launch {
                try {
                    if (hasPostIdParam) {
                        val result = updatePost(
                            Post(
                                _id = uiState.id,
                                title = uiState.title,
                                subtitle = uiState.subtitle,
                                thumbnail = uiState.thumbnail,
                                content = finalContent,
                                category = uiState.category,
                                popular = uiState.popular,
                                main = uiState.main,
                                sponsored = uiState.sponsored
                            )
                        )
                        if (result) {
                            context.router.navigateTo(Screen.AdminSuccess.postUpdated())
                        } else {
                            uiState = uiState.copy(messagePopup = true)
                        }
                    } else {
                        val result = addPost(
                            Post(
                                author = localStorage["username"] ?: "Unknown",
                                title = uiState.title,
                                subtitle = uiState.subtitle,
                                date = Date.now(),
                                thumbnail = uiState.thumbnail,
                                content = finalContent,
                                category = uiState.category,
                                popular = uiState.popular,
                                main = uiState.main,
                                sponsored = uiState.sponsored
                            )
                        )
                        if (result) {
                            context.router.navigateTo(Screen.AdminSuccess.route)
                        } else {
                            uiState = uiState.copy(messagePopup = true)
                        }
                    }
                } catch (e: Exception) {
                    console.log("Error during sticky post submission:", e.message)
                    uiState = uiState.copy(messagePopup = true)
                }
            }
        } else {
            val missingFields = mutableListOf<String>()
            if (uiState.title.isEmpty()) missingFields.add("Title")
            if (uiState.subtitle.isEmpty()) missingFields.add("Subtitle")
            if (uiState.thumbnail.isEmpty()) missingFields.add("Thumbnail")
            if (finalContent.isEmpty()) missingFields.add("Content")
            console.log("Missing fields (sticky): ${missingFields.joinToString(", ")}")
            scope.launch {
                uiState = uiState.copy(messagePopup = true)
                delay(3000)
                uiState = uiState.copy(messagePopup = false)
            }
        }
    }
}

@Composable
fun CategoryDropdown(
    selectedCategory: Category,
    onCategorySelect: (Category) -> Unit
) {
    Row(
        modifier = Modifier
            .margin(topBottom = 12.px)
            .fillMaxWidth()
            .height(54.px)
            //.backgroundColor(JsTheme.LightGray.rgb)
            .borderRadius(r = 4.px)

        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        org.jetbrains.compose.web.dom.Select(
            attrs = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .fontSize(16.px)
                .fontFamily(FONT_FAMILY)
                .backgroundColor(JsTheme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .padding(leftRight = 20.px)
                .noBorder() // Remove border line
                .toAttrs {
                    // Keep value as an attribute so the select reflects `selectedCategory` state
                    // Use the Compose-controlled value API so the select updates with state
                    // value(selectedCategory.name)
                    // Use attribute binding so the select reflects `selectedCategory` state
                    attr("value", selectedCategory.name)
                     onChange { event ->
                         val value = event.value
                         val category = Category.entries.find { it.name == value }
                         if (category != null) {
                             onCategorySelect(category)
                         }
                     }
                 }
        ) {
            Category.entries.forEach { category ->
                org.jetbrains.compose.web.dom.Option(
                    value = category.name
                ) {
                    Text(category.name)
                }
            }
        }
    }
}

@Composable
fun ThumbnailUploader(
    thumbnail: String,
    thumbnailInputDisabled: Boolean,
    onThumbnailSelect: (String, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 20.px)
            .height(54.px)
    ) {
        Input(
            type = InputType.Text,
            attrs = Modifier
                .id(Id.thumbnailInput)
                .fillMaxSize()
                .margin(right = 12.px)
                .padding(leftRight = 20.px)
                .backgroundColor(JsTheme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .noBorder()
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                // disable the text input when in upload mode
                .thenIf(
                    condition = thumbnailInputDisabled,
                    other = Modifier.disabled()
                )
                .toAttrs {
                    attr("placeholder", "Thumbnail")
                    // Use controlled value binding so input reflects `thumbnail` state
                    // Use the Compose-controlled value API so the input updates with state
                    value(thumbnail)
                     if (!thumbnailInputDisabled) {
                        onInput { event ->
                            console.log("Thumbnail URL input ->", event.value)
                            onThumbnailSelect(event.value, event.value)
                        }
                     }
                 }
        )
        Button(
            attrs = Modifier
                .onClick {
                    console.log("Upload button clicked; thumbnailInputDisabled=", thumbnailInputDisabled)
                    // open file picker only when upload mode is active (i.e., text input is disabled)
                    if (thumbnailInputDisabled) {
                        // In our semantics, thumbnailInputDisabled == true means text input disabled -> upload mode
                        // Use the library function correctly: pass `document` as the receiver and capture the load context and data URL
                        // Call as an extension on Document. onLoad is a LoadContext.(String) -> Unit receiver lambda,
                        // so `this` inside the lambda is the LoadContext and receives the data URL as the parameter.
                        document.loadDataUrlFromDisk("image/png, image/jpeg", { /* onError -> ignore */ }) { dataUrl ->
                            // The lambda is a receiver-style callback where the receiver contains file metadata.
                            // Safely access the filename from the receiver using dynamic access so the property is stable
                            // across compiler name-mangling and to avoid referencing an undefined local variable.
                            val filename = (this.asDynamic().filename as? String) ?: "unknown"
                            console.log("Loaded file -> filename:", filename)
                            onThumbnailSelect(filename, dataUrl)
                        }
                     }
                 }
                 .fillMaxHeight()
                 .padding(leftRight = 24.px)
                 // style as active when upload mode (thumbnailInputDisabled == true)
                 .backgroundColor(if (thumbnailInputDisabled) JsTheme.Primary.rgb else JsTheme.Gray.rgb)
                 .color(if (thumbnailInputDisabled) Colors.White else JsTheme.DarkGray.rgb)
                 .borderRadius(r = 4.px)
                 .noBorder()
                 .fontFamily(FONT_FAMILY)
                 .fontWeight(FontWeight.Medium)
                 .fontSize(14.px)
                 .cursor(if (thumbnailInputDisabled) Cursor.Pointer else Cursor.NotAllowed)
                 .thenIf(
                     condition = !thumbnailInputDisabled,
                     other = Modifier.disabled()
                 )
                .toAttrs()
        ) {
            SpanText(text = "Upload")
        }
    }
}

@Composable
fun EditorControls(
    breakpoint: Breakpoint,
    editorVisibility: Boolean,
    editorContent: String,
    onLinkClick: () -> Unit,
    onImageClick: () -> Unit,
    onEditorVisibilityChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        SimpleGrid(
            modifier = Modifier.fillMaxWidth(),
            numColumns = numColumns(base = 1, sm = 2)
        ) {
            Row(
                modifier = Modifier
                    .backgroundColor(JsTheme.LightGray.rgb)
                    .borderRadius(r = 4.px)
                    .height(54.px)
            ) {
                EditorControl.entries.forEach {
                    EditorControlView(
                        control = it,
                        onClick = {
                            console.log("Editor control clicked ->", it.name)
                            applyControlStyle(
                                editorControl = it,
                                onLinkClick = onLinkClick,
                                onImageClick = onImageClick
                            )
                        }
                    )
                }
            }
            Box(contentAlignment = Alignment.CenterEnd) {
                Button(
                    attrs = Modifier
                        .height(54.px)
                        .thenIf(
                            condition = breakpoint < Breakpoint.SM,
                            other = Modifier.fillMaxWidth()
                        )
                        .margin(topBottom = if (breakpoint < Breakpoint.SM) 12.px else 0.px)
                        .padding(leftRight = 24.px)
                        .borderRadius(r = 4.px)
                        .backgroundColor(
                            if (editorVisibility) JsTheme.LightGray.rgb
                            else JsTheme.Primary.rgb
                        )
                        .color(
                            if (editorVisibility) JsTheme.DarkGray.rgb
                            else Colors.White
                        )
                        .noBorder()
                        .onClick {
                            // Use the provided editorContent instead of reading DOM
                            onEditorVisibilityChange(editorContent)
                        }
                        .toAttrs()
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontWeight(FontWeight.Medium)
                            .fontSize(14.px),
                        text = "Preview"
                    )
                }
            }
        }
    }
}

@Composable
fun EditorControlView(
    control: EditorControl,
    onClick: () -> Unit
) {
    Box(
        modifier = EditorKeyStyle.toModifier()
            .fillMaxHeight()
            .padding(leftRight = 12.px)
            .borderRadius(r = 4.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            src = control.icon,
            alt = "${control.name} Icon"
        )
    }
}

@Composable
fun Editor(
    editorVisibility: Boolean,
    content: String = "",
    onContentChange: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextArea(
            attrs = Modifier
                .id(Id.editor)
                .fillMaxWidth()
                .height(400.px)
                .maxHeight(400.px)
                .resize(Resize.None)
                .margin(top = 8.px)
                .padding(all = 20.px)
                .backgroundColor(JsTheme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .noBorder()
                .visibility(
                    if (editorVisibility) Visibility.Visible
                    else Visibility.Hidden
                )
                .onKeyDown {
                    if (it.code == "Enter" && it.shiftKey) {
                        applyStyle(
                            controlStyle = ControlStyle.Break(
                                selectedText = getSelectedText()
                            )
                        )
                    }
                }
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .toAttrs {
                    attr("placeholder", "Type here...")
                    value(content)
                    onInput { event ->
                        onContentChange(event.value)
                    }
                }
        )
        Div(
            attrs = Modifier
                .id(Id.editorPreview)
                .fillMaxWidth()
                .height(400.px)
                .maxHeight(400.px)
                .margin(top = 8.px)
                .padding(all = 20.px)
                .backgroundColor(JsTheme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .visibility(
                    if (editorVisibility) Visibility.Hidden
                    else Visibility.Visible
                )
                .overflow(Overflow.Auto)
                .scrollBehavior(ScrollBehavior.Smooth)
                .noBorder()
                .toAttrs()
        )
    }
}

@Composable
fun CreateButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        attrs = Modifier
            .onClick {
                console.log("Create button physically clicked!")
                onClick()
            }
            .fillMaxWidth()
            .height(54.px)
            .margin(top = 24.px, bottom = 50.px) // Add bottom margin for visibility
            .backgroundColor(JsTheme.Primary.rgb)
            .color(Colors.White)
            .borderRadius(r = 4.px)
            .noBorder()
            .fontFamily(FONT_FAMILY)
            .fontSize(16.px)
            .fontWeight(FontWeight.Bold) // Make text bold
            .cursor(Cursor.Pointer) // Ensure cursor changes
            .toAttrs()
    ) {
        SpanText(text = text)
    }
}

@Composable
fun StickyCreateBar(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .styleModifier {
                property("position", "fixed")
                property("bottom", "20px")
                property("left", "0")
                property("z-index", "2000")
                property("display", "flex")
                property("justify-content", "center")
                property("pointer-events", "none")
            }
    ) {
        Box(
            modifier = Modifier
                .styleModifier {
                    property("pointer-events", "auto")
                }
                .maxWidth(700.px)
        ) {
            CreateButton(
                text = text,
                onClick = onClick
            )
        }
    }
}
