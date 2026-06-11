/* ═══════════════════════════════════════
   BLOK v6 — Script
   ═══════════════════════════════════════ */
(() => {
  'use strict';

  const isMobile = !window.matchMedia('(pointer:fine)').matches;

  /* ── Loader ── */
  window.addEventListener('load', () => {
    const loader = document.getElementById('loader');
    if (loader) setTimeout(() => loader.classList.add('done'), 1000);
  });

  /* ── Nav ── */
  const nav = document.getElementById('nav');
  let ly = 0;
  window.addEventListener('scroll', () => {
    const y = window.scrollY;
    nav.classList.toggle('hide', y > 60 && y > ly);
    ly = y;
  }, { passive: true });

  /* ── Burger ── */
  const burger = document.getElementById('burger');
  const mm = document.getElementById('mmenu');
  if (burger && mm) {
    burger.addEventListener('click', () => {
      burger.classList.toggle('active');
      mm.classList.toggle('open');
      document.body.style.overflow = mm.classList.contains('open') ? 'hidden' : '';
    });
    mm.querySelectorAll('a').forEach(a => a.addEventListener('click', () => {
      burger.classList.remove('active');
      mm.classList.remove('open');
      document.body.style.overflow = '';
    }));
  }

  /* ── Hero exit parallax: copy and 3D drift at different rates ── */
  const hCopy = document.querySelector('.h-copy');
  const hVis = document.querySelector('.h-visual');
  const hBig = document.querySelector('.h-big');
  if (hCopy && !isMobile) {
    let raf = 0;
    window.addEventListener('scroll', () => {
      if (raf) return;
      raf = requestAnimationFrame(() => {
        raf = 0;
        const y = window.scrollY;
        const h = innerHeight;
        if (y < h) {
          const t = y / h;
          hCopy.style.transform = `translateY(${y * 0.18}px)`;
          hCopy.style.opacity = 1 - t * 1.1;
          if (hVis) { hVis.style.transform = `translateY(${y * 0.08}px)`; hVis.style.opacity = 1 - t * 0.9; }
          if (hBig) hBig.style.transform = `translateY(calc(-50% + ${y * 0.28}px))`;
        }
      });
    }, { passive: true });
  }

  /* ── Reveal ── */
  const obs = new IntersectionObserver(entries => {
    entries.forEach(en => {
      if (en.isIntersecting) {
        const p = en.target.parentElement;
        const sibs = p ? [...p.querySelectorAll(':scope > .rv')] : [];
        const i = sibs.indexOf(en.target);
        setTimeout(() => en.target.classList.add('v'), Math.max(0, i) * 80);
        obs.unobserve(en.target);
      }
    });
  }, { threshold: 0.06, rootMargin: '0px 0px -30px 0px' });
  document.querySelectorAll('.rv').forEach(el => obs.observe(el));

  /* ── FAQ ── */
  document.querySelectorAll('.fq-q').forEach(btn => {
    btn.addEventListener('click', () => {
      const item = btn.closest('.fq');
      const open = item.classList.contains('open');
      document.querySelectorAll('.fq.open').forEach(i => { if (i !== item) i.classList.remove('open'); });
      item.classList.toggle('open', !open);
    });
  });

  /* ── Smooth scroll ── */
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', e => {
      const href = a.getAttribute('href');
      if (href === '#') return;
      const t = document.querySelector(href);
      if (t) { e.preventDefault(); t.scrollIntoView({ behavior: 'smooth', block: 'start' }); }
    });
  });

  /* ══════════════════════════════════════
     MOUSE TRAIL — ssscript technique:
     soft light splats painted into a buffer
     that dissipates every frame (uDissipate)
     ══════════════════════════════════════ */
  if (!isMobile) {
    const c = document.getElementById('trail');
    const ctx = c.getContext('2d');
    let W, H;

    let mx = -999, my = -999;   // raw mouse
    let px = -999, py = -999;   // previous splat position
    let cx = -999, cy = -999;   // smoothed cursor dot

    function resize() {
      W = c.width = innerWidth;
      H = c.height = innerHeight;
    }
    resize();
    addEventListener('resize', resize);

    document.addEventListener('mousemove', e => {
      mx = e.clientX; my = e.clientY;
      if (px < -900) { px = mx; py = my; cx = mx; cy = my; }
    });

    // Particle splats: the canvas is fully cleared and redrawn every frame,
    // and each splat dies after LIFE ms — nothing can ever stay painted.
    const LIFE = 450;
    const splats = [];

    function tick() {
      const now = performance.now();
      ctx.clearRect(0, 0, W, H);

      // spawn splats along the mouse path
      if (px > -900) {
        const dx = mx - px, dy = my - py;
        const dist = Math.hypot(dx, dy);
        if (dist > 0.5) {
          const speed = Math.min(dist, 90);
          const radius = 13 + speed * 0.38;
          const steps = Math.max(1, Math.min(Math.ceil(dist / (radius * 0.4)), 12));
          for (let i = 1; i <= steps; i++) {
            splats.push({ x: px + dx * i / steps, y: py + dy * i / steps, r: radius, born: now });
          }
        }
        px = mx; py = my;
      }
      if (splats.length > 260) splats.splice(0, splats.length - 260);

      // draw living splats, alpha eased to exact zero at end of life
      ctx.globalCompositeOperation = 'lighter';
      for (let i = splats.length - 1; i >= 0; i--) {
        const s = splats[i];
        const t = (now - s.born) / LIFE;
        if (t >= 1) { splats.splice(i, 1); continue; }
        const a = 0.05 * (1 - t) * (1 - t);
        const r = s.r * (1 + t * 0.6);
        const g = ctx.createRadialGradient(s.x, s.y, 0, s.x, s.y, r);
        g.addColorStop(0, `rgba(245,255,0,${a})`);
        g.addColorStop(0.55, `rgba(245,255,0,${a * 0.35})`);
        g.addColorStop(1, 'rgba(245,255,0,0)');
        ctx.fillStyle = g;
        ctx.beginPath();
        ctx.arc(s.x, s.y, r, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.globalCompositeOperation = 'source-over';

      // cursor dot + halo
      cx += (mx - cx) * 0.35;
      cy += (my - cy) * 0.35;
      if (cx > -900) {
        ctx.beginPath();
        ctx.arc(cx, cy, 3, 0, Math.PI * 2);
        ctx.fillStyle = '#F5FF00';
        ctx.fill();
        const rg = ctx.createRadialGradient(cx, cy, 0, cx, cy, 22);
        rg.addColorStop(0, 'rgba(245,255,0,0.10)');
        rg.addColorStop(1, 'rgba(245,255,0,0)');
        ctx.beginPath();
        ctx.arc(cx, cy, 22, 0, Math.PI * 2);
        ctx.fillStyle = rg;
        ctx.fill();
      }

      requestAnimationFrame(tick);
    }
    tick();
  }


  /* ── Stats counter animation ── */
  const statObs = new IntersectionObserver(entries => {
    entries.forEach(en => {
      if (en.isIntersecting) {
        const el = en.target;
        const text = el.textContent;
        const numMatch = text.match(/[\d.]+/);
        if (!numMatch) return;
        const target = parseFloat(numMatch[0]);
        const suffix = el.querySelector('span');
        const suffixText = suffix ? suffix.textContent : '';
        const isDecimal = text.includes('.');
        const duration = 1200;
        const start = performance.now();

        function animate(now) {
          const t = Math.min((now - start) / duration, 1);
          const eased = 1 - Math.pow(1 - t, 3);
          const val = target * eased;
          const display = isDecimal ? val.toFixed(1) : Math.round(val);
          el.childNodes[0].textContent = display;
          if (t < 1) requestAnimationFrame(animate);
        }
        requestAnimationFrame(animate);
        statObs.unobserve(el);
      }
    });
  }, { threshold: 0.5 });
  document.querySelectorAll('.stat-val').forEach(el => statObs.observe(el));

})();
