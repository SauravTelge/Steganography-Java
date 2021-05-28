
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Steganography
{
        public Steganography()
        {
        }
        public boolean encode(String file_path, String message)
        {
                
                BufferedImage image = user_space(getImage(file_path));
                image = add_text(image,message);
                return(setImage(image,new File(file_path),"png"));
        }
        
        public String decode(String file_path)
        {
                byte[] decode;
                try
                {
                        BufferedImage image  = user_space(getImage(file_path));
                        decode = decode_text(get_byte_data(image));
                        return(new String(decode));
                }
                catch(Exception e)
                {
                        JOptionPane.showMessageDialog(null, "There is no hidden message in this image!","Error",JOptionPane.ERROR_MESSAGE);
                        return "";
                }
        }
        private BufferedImage getImage(String f)//BufferedImage method used to get Image into the buffered file
        {
                BufferedImage image = null;//temporary varaible initialised
                File file = new File(f);//New file declared with file variable
                
                try
                {
                        image = ImageIO.read(file);//image variable loaded with the image file
                }
                catch(Exception ex)
                {
                        JOptionPane.showMessageDialog(null, "Image could not be read!","Error",JOptionPane.ERROR_MESSAGE);
                }
                return image;//imsge returned
        }
        
        private boolean setImage(BufferedImage image, File file, String extension)//SetImage method used
        {
                try
                {
                        ImageIO.write(image,extension,file);//ImageIO file used to write image with its extension and file data
                        return true;
                }
                catch(Exception e)//catch statement
                {
                        JOptionPane.showMessageDialog(null, "File could not be saved!","Error",JOptionPane.ERROR_MESSAGE);
                        return false;
                }
        }
        private BufferedImage add_text(BufferedImage image, String text)
        {
                //convert all items to byte arrays: image, message, message length
                byte img[]  = get_byte_data(image);
                byte msg[] = text.getBytes();
                byte len[]   = bit_conversion(msg.length);
                try
                {
                        encode_text(img, len,  0); //0 first positioning
                        encode_text(img, msg, 32); //4 bytes of space for length: 4bytes*8bit = 32 bits
                }
                catch(Exception e)
                {
                        JOptionPane.showMessageDialog(null, "Target File cannot hold message!", "Error",JOptionPane.ERROR_MESSAGE);
                }
                return image;
        }
        
        private BufferedImage user_space(BufferedImage image)//class BufferedImage called
        {
                //create new_img with the attributes of image
                BufferedImage new_img  = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphics = new_img.createGraphics();
                graphics.drawRenderedImage(image, null);
                return new_img;
        }
        
        private byte[] get_byte_data(BufferedImage image)//Used to convert image into byte form
        {
                WritableRaster raster = image.getRaster();
                DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
                return buffer.getData();
        }
        

        private byte[] bit_conversion(int i)
        {             
                //only using 4 bytes
                byte byte3 = (byte)((i & 0xFF000000) >>> 24); //0
                byte byte2 = (byte)((i & 0x00FF0000) >>> 16); //0
                byte byte1 = (byte)((i & 0x0000FF00) >>> 8 ); //0
                byte byte0 = (byte)((i & 0x000000FF)       );
                //{0,0,0,byte0} is equivalent, since all shifts >=8 will be 0
                return(new byte[]{byte3,byte2,byte1,byte0});
        }
        

        private byte[] encode_text(byte[] image, byte[] addition, int offset)
        {
                //check that the data + offset will fit in the image
                if(addition.length + offset > image.length)
                {
                        throw new IllegalArgumentException("File not long enough!");
                }
                //loop through each addition byte
                for(int i=0; i<addition.length; ++i)
                {
                        //loop through the 8 bits of each byte
                        int add = addition[i];
                        for(int bit=7; bit>=0; --bit, ++offset) //ensure the new offset value carries on through both loops
                        {
                                //assign an integer to b, shifted by bit spaces AND 1
                                //a single bit of the current byte
                                int b = (add >>> bit) & 1;
                                //assign the bit by taking: [(previous byte value) AND 0xfe] OR bit to add
                                //changes the last bit of the byte in the image to be the bit of addition
                                image[offset] = (byte)((image[offset] & 0xFE) | b );
                        }
                }
                return image;
        }
        
        private byte[] decode_text(byte[] image)
        {
                int length = 0;
                int offset  = 32;
                //loop through 32 bytes of data to determine text length
                for(int i=0; i<32; ++i) 
                {
                        length = (length << 1) | (image[i] & 1);
                }
                
                byte[] result = new byte[length];
                
                //loop through each byte of text
                for(int b=0; b<result.length; ++b )
                {
                        //loop through each bit within a byte of text
                        for(int i=0; i<8; ++i, ++offset)
                        {
                                //assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
                                result[b] = (byte)((result[b] << 1) | (image[offset] & 1));
                        }
                }
                return result;
        }
}