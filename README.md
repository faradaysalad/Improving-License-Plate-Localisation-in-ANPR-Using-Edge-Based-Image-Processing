# 🚘 Improving License Plate Localisation in Automatic Number Plate Recognition Using Edge-Based Image Processing

A Java-based Automatic Number Plate Recognition (ANPR) localisation system that improves license plate detection consistency using classical edge-based image processing techniques without relying on AI or deep learning models.

---

## 📌 Project Overview

This project focuses on enhancing the **license plate localisation stage** in Automatic Number Plate Recognition (ANPR) systems through structured image preprocessing and edge-based segmentation techniques.

Unlike modern AI-based approaches, this system leverages efficient classical computer vision methods such as:

- Grayscale preprocessing
- Gaussian filtering
- Canny edge detection
- Morphological processing
- Thinning operations
- Connected component analysis

The system is implemented as a **desktop-based Java application** with a graphical user interface (GUI) for image loading and localisation visualisation.

---

# 🖼️ System Architecture

```text
Input Vehicle Image
        ↓
Grayscale Conversion
        ↓
Gaussian Filtering
        ↓
Image Enhancement
        ↓
Canny Edge Detection
        ↓
Thinning Process
        ↓
Morphological Processing
        ↓
Connected Component Analysis
        ↓
License Plate Localisation Output
```

---

# 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core system development |
| Java Swing | GUI implementation |
| Digital Image Processing | Image enhancement & segmentation |
| Canny Edge Detection | Edge extraction |
| Morphological Operations | Region refinement |
| Connected Component Analysis | Candidate region selection |

---

# 📂 Project Structure

```bash
anpr-license-plate-localisation/
│
├── src/
│   ├── gui/
│   ├── processing/
│   ├── detection/
│   └── utils/
│
├── images/
│   ├── input/
│   └── output/
│
├── report/
│   └── ANPR_FinalReport_GP2.pdf
│
├── README.md
└── LICENSE
```

---

# 🚀 Getting Started

## Prerequisites

Before running the project, ensure you have:

- Java JDK 8 or above
- IntelliJ IDEA / NetBeans / Eclipse
- Git installed

---

## Installation

Clone the repository:

```bash
git clone https://github.com/your-username/anpr-license-plate-localisation.git
```

Navigate into the project folder:

```bash
cd anpr-license-plate-localisation
```

Open the project in your preferred Java IDE.

Run the main application file.

---

# ⚙️ System Workflow

## 1. Image Acquisition
Vehicle images are loaded into the system through the GUI interface.

## 2. Grayscale Conversion
Input images are converted into grayscale to reduce computational complexity and improve edge extraction.

## 3. Gaussian Filtering
Gaussian filtering is applied to reduce image noise and smooth intensity transitions.

## 4. Image Enhancement
Contrast enhancement techniques improve the visibility of license plate features.

## 5. Edge Detection
Canny edge detection identifies strong structural edges corresponding to plate boundaries and characters.

## 6. Thinning Process
Detected edges are refined into single-pixel-wide structures for improved segmentation clarity.

## 7. Morphological Processing
Dilation and erosion operations connect fragmented edges and remove unwanted regions.

## 8. Connected Component Analysis
Candidate regions are analysed using geometric properties such as:
- Aspect ratio
- Bounding box dimensions
- Region size

## 9. License Plate Localisation
The most suitable candidate region is selected and displayed as the final localisation result.

---

# 📸 Sample Results

## Original Vehicle Image

_Add screenshot here_

## Localised License Plate Output

_Add screenshot here_

---

# 📊 Research Contributions

This project introduces several improvements to traditional edge-based ANPR localisation systems:

- Enhanced grayscale preprocessing
- Improved edge refinement using thinning
- Structured morphological processing
- Connected component-based segmentation
- Improved localisation consistency under varying lighting conditions
- Low computational complexity suitable for lightweight systems and academic use

---

# 📈 Comparison with Traditional Vertical Edge-Based Methods

| Aspect | Traditional Method | Proposed Method |
|---|---|---|
| Preprocessing | Basic filtering | Enhanced grayscale preprocessing |
| Edge Detection | Vertical edge focused | General edge detection + thinning |
| Morphological Processing | Limited | Structured refinement |
| Segmentation | Edge density-based | Connected component analysis |
| Robustness | Sensitive to noise | Improved stability |
| GUI Support | No | Yes |

---

# 🔬 Experimental Findings

Experimental evaluation demonstrates that:

- Grayscale preprocessing improves edge consistency
- Gaussian filtering reduces unwanted noise
- Morphological refinement improves candidate segmentation
- Connected component analysis enhances localisation reliability
- The system performs consistently across varying image conditions

The proposed approach improves localisation robustness while maintaining low computational complexity.

---

# 🔮 Future Improvements

Potential future enhancements include:

- Character segmentation
- Optical Character Recognition (OCR)
- Real-time ANPR video processing
- Deep learning integration
- Hybrid AI-assisted localisation
- Improved performance in extreme lighting conditions

---

# 👨‍💻 Authors

- **Farah Dania Binti Imam Nawawi** (A205566)
- Muhammad Dani Ilhan Bin Abd Gani
- Muhammad Nur Luqman Bin Zamry

---


# 📄 License

This project is developed for academic and research purposes only.

---

# 🤝 Acknowledgements

Special thanks to:

- Universiti Kebangsaan Malaysia (UKM)
- Faculty of Information Science and Technology

---

# ⭐ Support

If you found this project useful, consider giving this repository a ⭐ on GitHub.
