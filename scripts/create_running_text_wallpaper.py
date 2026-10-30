import os
import zipfile
import io
from PIL import Image, ImageDraw, ImageFont

def create_marquee_frames(text, num_frames=20, width=110, height=110, text_color=(0, 255, 200), bg_color=(0, 0, 0, 0)):
    """
    Menghasilkan list PIL Image berisi animasi teks berjalan horizontal (marquee)
    yang kompatibel dengan palet/format watch face.
    """
    frames = []
    
    # Coba gunakan font default atau truetype jika ada
    try:
        font = ImageFont.truetype("arial.ttf", 22)
    except:
        font = ImageFont.load_default()

    # Hitung lebar total teks
    dummy_img = Image.new("RGBA", (1, 1))
    draw = ImageDraw.Draw(dummy_img)
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    # Total jarak tempuh pergeseran teks
    total_distance = text_width + width
    step = total_distance / num_frames

    for i in range(num_frames):
        img = Image.new("RGBA", (width, height), bg_color)
        draw = ImageDraw.Draw(img)
        
        # Posisi X bergerak dari kanan (width) ke kiri (-text_width)
        x = width - int(i * step)
        y = (height - text_height) // 2
        
        draw.text((x, y), text, font=font, fill=text_color)
        
        # Konversi ke mode 'P' (Paletted) dengan transparansi untuk hemat ukuran
        img_p = img.quantize(colors=64, method=Image.Quantize.FASTOCTREE)
        frames.append(img_p)

    return frames

def build_running_text_watchface(template_zip_path, output_zip_path, custom_text="TRI WAHYU • ACTIVE 5"):
    """
    Menyuntikkan frame teks berjalan ke dalam template data.zip watch face.
    """
    if not os.path.exists(template_zip_path):
        raise FileNotFoundError(f"Template zip tidak ditemukan di {template_zip_path}")

    print(f"[*] Membuat 20 frame running text: '{custom_text}'")
    frames = create_marquee_frames(custom_text, num_frames=20, width=110, height=110)

    with zipfile.ZipFile(template_zip_path, 'r') as zin:
        with zipfile.ZipFile(output_zip_path, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                # Cek jika item adalah salah satu dari 20 frame animasi di slot 1
                if item.filename.startswith("face_data_1_1_") and item.filename.endswith(".png"):
                    # Ambil nomor frame
                    frame_num_str = item.filename.replace("face_data_1_1_", "").replace(".png", "")
                    try:
                        frame_idx = int(frame_num_str) - 1
                        if 0 <= frame_idx < len(frames):
                            buf = io.BytesIO()
                            frames[frame_idx].save(buf, format="PNG")
                            zout.writestr(item, buf.getvalue())
                            continue
                    except ValueError:
                        pass
                
                # Default copy file original
                zout.writestr(item, zin.read(item.filename))

    print(f"[+] Berhasil membuat watch face kustom: {output_zip_path}")

if __name__ == "__main__":
    template = "Redmi_Watch_5_Active_Reverse_Engineering/firmware/sample_watchface.zip"
    output = "Redmi_Watch_5_Active_Reverse_Engineering/firmware/running_text_watchface.zip"
    build_running_text_watchface(template, output, "TRI WAHYU • REDMI WATCH 5 ACTIVE")
