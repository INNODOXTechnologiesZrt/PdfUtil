package com.innodox.model;

public class PdfAttachment {
    private byte[] data;
    private String filename;
    private String description;

    public PdfAttachment(byte[] data, String filename, String description) {
        this.data = data;
        this.filename = filename;
        this.description = description;
    }

    public PdfAttachment() {
    }

    public static PdfAttachmentBuilder builder() {
        return new PdfAttachmentBuilder();
    }

    public byte[] getData() {
        return this.data;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getDescription() {
        return this.description;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PdfAttachment)) return false;
        final PdfAttachment other = (PdfAttachment) o;
        if (!other.canEqual((Object) this)) return false;
        if (!java.util.Arrays.equals(this.getData(), other.getData())) return false;
        final Object this$filename = this.getFilename();
        final Object other$filename = other.getFilename();
        if (this$filename == null ? other$filename != null : !this$filename.equals(other$filename)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PdfAttachment;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.hashCode(this.getData());
        final Object $filename = this.getFilename();
        result = result * PRIME + ($filename == null ? 43 : $filename.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    public String toString() {
        return "PdfAttachment(data=" + java.util.Arrays.toString(this.getData()) + ", filename=" + this.getFilename() + ", description=" + this.getDescription() + ")";
    }

    public static class PdfAttachmentBuilder {
        private byte[] data;
        private String filename;
        private String description;

        PdfAttachmentBuilder() {
        }

        public PdfAttachmentBuilder data(byte[] data) {
            this.data = data;
            return this;
        }

        public PdfAttachmentBuilder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public PdfAttachmentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PdfAttachment build() {
            return new PdfAttachment(this.data, this.filename, this.description);
        }

        public String toString() {
            return "PdfAttachment.PdfAttachmentBuilder(data=" + java.util.Arrays.toString(this.data) + ", filename=" + this.filename + ", description=" + this.description + ")";
        }
    }
}
