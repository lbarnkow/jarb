package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import static com.google.common.truth.Truth.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.lbarnkow.jarb.api.Attachment;

class RawAttachmentTest {

	@Test
	void testConvertList() {
		// given
		List<RawAttachment> rawAttachments = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			rawAttachments.add(createRawAttachment());
		}

		// when
		List<Attachment> attachments = RawAttachment.convertList(rawAttachments);

		// then
		assertThat(attachments.size()).isEqualTo(rawAttachments.size());
		for (int i = 0; i < attachments.size(); i++) {
			assertEqual(rawAttachments.get(i), attachments.get(i));
		}
	}

	@Test
	void testConvertEmptyList() {
		// given

		// when
		List<Attachment> attachments1 = RawAttachment.convertList(Collections.emptyList());
		List<Attachment> attachments2 = RawAttachment.convertList(null);

		// then
		assertThat(attachments1).isEmpty();
		assertThat(attachments2).isEmpty();
	}

	@Test
	void testOfList() {
		// given
		List<Attachment> attachments = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			attachments.add(createAttachment());
		}

		// when
		List<RawAttachment> rawAttachments = RawAttachment.of(attachments);

		// then
		assertThat(attachments.size()).isEqualTo(rawAttachments.size());
		for (int i = 0; i < attachments.size(); i++) {
			assertEqual(rawAttachments.get(i), attachments.get(i));
		}
	}

	@Test
	void testOfEmptyList() {
		// given

		// when
		List<RawAttachment> rawAttachments1 = RawAttachment.of(Collections.emptyList());
		List<RawAttachment> rawAttachments2 = RawAttachment.of((List<Attachment>) null);

		// then
		assertThat(rawAttachments1).isEmpty();
		assertThat(rawAttachments2).isEmpty();
	}

	private RawAttachment createRawAttachment() {
		return RawAttachment.builder() //
				.color("color") //
				.text("text") //
				.ts(Instant.now().toString()) //
				.thumb_url("thumb_url") //
				.message_link("message_link") //
				.collapsed(true) //
				.author_name("author_name") //
				.author_link("author_link") //
				.author_icon("author_icon") //
				.title("title") //
				.title_link("title_link") //
				.title_link_download(true) //
				.image_url("image_url") //
				.audio_url("audio_url") //
				.video_url("video_url") //
				.build();
	}

	private Attachment createAttachment() {
		return Attachment.builder() //
				.color("color") //
				.text("text") //
				.timestamp(Instant.now()) //
				.thumbUrl("thumb_url") //
				.messageLink("message_link") //
				.collapsed(true) //
				.authorName("author_name") //
				.authorLink("author_link") //
				.authorIcon("author_icon") //
				.title("title") //
				.titleLink("title_link") //
				.titleLinkDownload(true) //
				.imageUrl("image_url") //
				.audioUrl("audio_url") //
				.videoUrl("video_url") //
				.build();
	}

	private void assertEqual(RawAttachment rawAttachment, Attachment attachment) {
		assertThat(attachment.getColor()).isEqualTo(rawAttachment.getColor());
		assertThat(attachment.getText()).isEqualTo(rawAttachment.getText());
		assertThat(attachment.getTimestamp().toString()).isEqualTo(rawAttachment.getTs());
		assertThat(attachment.getThumbUrl()).isEqualTo(rawAttachment.getThumb_url());
		assertThat(attachment.getMessageLink()).isEqualTo(rawAttachment.getMessage_link());
		assertThat(attachment.isCollapsed()).isEqualTo(rawAttachment.isCollapsed());
		assertThat(attachment.getAuthorName()).isEqualTo(rawAttachment.getAuthor_name());
		assertThat(attachment.getAuthorLink()).isEqualTo(rawAttachment.getAuthor_link());
		assertThat(attachment.getAuthorIcon()).isEqualTo(rawAttachment.getAuthor_icon());
		assertThat(attachment.getTitle()).isEqualTo(rawAttachment.getTitle());
		assertThat(attachment.getTitleLink()).isEqualTo(rawAttachment.getTitle_link());
		assertThat(attachment.isTitleLinkDownload()).isEqualTo(rawAttachment.isTitle_link_download());
		assertThat(attachment.getImageUrl()).isEqualTo(rawAttachment.getImage_url());
		assertThat(attachment.getAudioUrl()).isEqualTo(rawAttachment.getAudio_url());
		assertThat(attachment.getVideoUrl()).isEqualTo(rawAttachment.getVideo_url());
	}

}
