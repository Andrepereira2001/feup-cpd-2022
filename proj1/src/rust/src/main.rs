use std::cmp;
use std::time::Instant;

fn main() {
    println!("Hello, rust!");

    for x in (600..3001).step_by(400){
        println!("\n\nStarting {} calculation", x);
        on_mult(x,x);
        on_mult2(x,x);
        on_mult_line(x,x);
    }
}

fn on_mult(m_ar: usize, m_br: usize){

    println!("on_mult Version 1");

    let mut pha = vec![0.0f64; m_ar*m_ar];
    for i in 0..m_ar{
        for j in 0..m_ar{
            pha[i * m_ar + j] = 1.0;
        }
    }

    let mut phb = vec![0.0f64; m_br*m_br];
    for i in 0..m_br{
        for j in 0..m_br{
            phb[i * m_br + j] = (i as f64) + 1.0;
        }
    }

    let mut phc = vec![0.0f64; m_ar*m_ar];

    let now = Instant::now();

    for i in 0..m_ar{
        for j in 0..m_br{
            let mut temp = 0.0f64;
            for k in 0..m_ar{
                temp += pha[i * m_ar + k] * phb[k * m_br + j]
            }
            phc[i * m_ar + j] = temp;
        }
    }

    let elapsed = now.elapsed();

    println!("Time: {:.3?}", elapsed);

    println!("Result matrix: ");
    for j in 0..cmp::min(10,m_br){
        print!("{} ", phc[j])
    }
    println!("")
}

fn on_mult2(m_ar: usize, m_br: usize){

    println!("on_mult Version 2");

    let mut pha = vec![0.0f64; m_ar*m_ar];
    for i in 0..m_ar{
        for j in 0..m_ar{
            pha[i * m_ar + j] = 1.0;
        }
    }

    let mut phb = vec![0.0f64; m_br*m_br];
    for i in 0..m_br{
        for j in 0..m_br{
            phb[i * m_br + j] = (j as f64) + 1.0;
        }
    }

    let mut phc = vec![0.0f64; m_ar*m_ar];

    let now = Instant::now();

    for i in 0..m_ar{
        for j in 0..m_br{
            let mut temp = 0.0f64;
            for k in 0..m_ar{
                temp += pha[i * m_ar + k] * phb[j * m_br + k]
            }
            phc[i * m_ar + j] = temp;
        }
    }

    let elapsed = now.elapsed();

    println!("Time: {:.3?}", elapsed);

    println!("Result matrix: ");
    for j in 0..cmp::min(10,m_br){
        print!("{} ", phc[j])
    }
    println!("")
}


fn on_mult_line(m_ar: usize, m_br: usize){

    println!("on_mult_line");

    let mut pha = vec![0.0f64; m_ar*m_ar];
    for i in 0..m_ar{
        for j in 0..m_ar{
            pha[i * m_ar + j] = 1.0;
        }
    }

    let mut phb = vec![0.0f64; m_br*m_br];
    for i in 0..m_br{
        for j in 0..m_br{
            phb[i * m_br + j] = (i as f64) + 1.0;
        }
    }

    let mut phc = vec![0.0f64; m_ar*m_ar];

    let now = Instant::now();

    for i in 0..m_ar{
        for k in 0..m_ar{
            for j in 0..m_br{
                phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
            }
        }
    }

    let elapsed = now.elapsed();

    println!("Time: {:.3?}", elapsed);

    println!("Result matrix: ");
    for j in 0..cmp::min(10,m_br){
        print!("{} ", phc[j])
    }
    println!("")
}