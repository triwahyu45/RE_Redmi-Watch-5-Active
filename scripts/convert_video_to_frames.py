import os
import sys
from PIL import Image

def process_gif(input_path, output_dir, target_width=320, target_height=385, max_frames=30):
    """
    Ekstrak frame dari GIF/Video dan resize sesuai layar Redmi Watch 5 Active (320x385).
    """
    if not os.path.exists(output_dir):
        os.makedirs(output_dir, exist_ok=True)

    img = Image.open(input_path)
    frame_count = 0

    print(f"[*] Memproses file: {input_path}")
    print(f"[*] Target resolusi: {target_width}x{target_height}")

    try:
        while True:
            # Resize frame dengan mempertahankan kualitas
            frame = img.copy().convert("RGBA")
            frame_resized = frame.resize((target_width, target_height), Image.Resampling.LANCZOS)
            
            output_file = os.path.join(output_dir, f"frame_{frame_count:03d}.png")
            frame_resized.save(output_file, format="PNG")
            
            frame_count += 1
            if max_frames and frame_count >= max_frames:
                print(f"[!] Mencapai batas maksimum frame ({max_frames} frame).")
                break

            img.seek(img.tell() + 1)
    except EOFError:
        pass  # Akhir dari frame GIF

    print(f"[+] Selesai! Sebanyak {frame_count} frame disimpan di: {output_dir}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Penggunaan: python convert_video_to_frames.py <path_ke_file_gif_atau_video> [max_frames]")
        print("Contoh: python convert_video_to_frames.py my_animation.gif 25")
        sys.exit(1)

    input_file = sys.argv[1]
    max_f = int(sys.argv[2]) if len(sys.argv) > 2 else 25
    out_dir = os.path.join(os.path.dirname(input_file), "extracted_frames")
    
    process_gif(input_file, out_dir, max_frames=max_f)
